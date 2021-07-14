package org.jetbrains.research.extractMethodExperiments.models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.research.extractMethodExperiments.csv.models.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.csv.models.ICSVItem;
import org.jetbrains.research.extractMethodExperiments.utils.feature.generators.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.jetbrains.research.extractMethodExperiments.utils.CodeFormattingUtils.*;
import static org.jetbrains.research.extractMethodExperiments.utils.feature.generators.DepthAnalyzer.getNestingArea;
import static org.jetbrains.research.extractMethodExperiments.utils.feature.generators.DepthAnalyzer.getNestingDepth;

public class Method {
    private final MethodDeclaration methodDeclaration;
    /**
     * repo from which the fragment was taken
     */
    private final Repository repo;
    private final String repoName;
    /**
     * path to the file from which the fragment was taken
     */
    private final String filePath;
    private List<Statement> statements;
    private final String initialMethod;
    private final Logger logger;
    private final int methodDepth;
    private final int methodArea;
    private final int methodBeginLine;
    private final int methodEndLine;

    public Method(Node mDec, Repository repo, String repoName, String filePath, Logger logger) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.repo = repo;
        this.repoName = repoName;
        this.filePath = filePath;
        if(methodDeclaration.getBody().isPresent())
            this.statements = getSubStatements((methodDeclaration.getBody().get()));
        this.methodBeginLine = mDec.getBegin().get().line;
        this.methodEndLine = mDec.getEnd().get().line;
        this.initialMethod = getCodeByLines(methodBeginLine, methodEndLine);
        this.logger = logger;
        this.methodArea = getNestingArea(initialMethod);
        this.methodDepth = getNestingDepth(initialMethod);
    }

    private String getCodeByLines(int beginLine, int endLine){
        List<String> methodLines = new ArrayList<>();
        try {
            List<String> lineStream = Files.readAllLines(Paths.get(repoName + "/" + filePath));
            for (int i = beginLine; i <= endLine; i++) {
                methodLines.add(lineStream.get(i - 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clearCode(String.join("\n", methodLines));
    }

    public final String getInitialMethod() {
        return initialMethod;
    }

    public final int getMethodDepth() {
        return methodDepth;
    }

    public final int getMethodArea() {
        return methodArea;
    }

    public final Repository getRepository() {
        return repo;
    }

    public final String getFilePath() {
        return filePath;
    }

    /**
     * Used to consider only statements ending with ; or }
     */
    private boolean isValidStatement(Statement s) {
        String linearText = clearCode(s.toString());
        linearText = linearText.replace("\n", "").replace("\r", "");
        return Pattern.matches(".*}", linearText) | Pattern.matches(".*;", linearText);
    }

    private List<Statement> getSubStatements(Statement s) {
        List<Statement> statements = new ArrayList<>();
        for (Node node : s.getChildNodes()) {
            if (node instanceof Statement) {
                if (isValidStatement((Statement) node))
                    statements.add((Statement) node);
            }
        }
        return statements;
    }

    public void processFragment(int threshold, FileWriter fw) {
        if(this.statements != null)
            processStatements(threshold, this.statements, fw);
    }

    /**
     * Splits fragment into sequences of statements of length
     * greater than `threshold`, and processes each such
     * sequence, generating a row to the specified file
     */
    private void processStatements(int threshold, List<Statement> context, FileWriter fw) {
        // Cycle for parsing embedded statements

        for (Statement s : context) {
            if (!s.getChildNodes().isEmpty()) {
                processStatements(threshold, getSubStatements(s), fw);
            }
        }
        NodeList<Statement> statementSequence = new NodeList<>();

        // Don't analyze too short methods
        if (context.size() >= threshold) {
            BlockStmt newBlock;
            for (int shift = 0; shift <= context.size() - threshold; shift++) {
                int beginLine = context.get(shift).getBegin().get().line;

                for (int i = 0; i < threshold - 1; i++) {
                    statementSequence.add(context.get(i + shift));
                }
                for (int j = threshold - 1; j + shift < context.size(); j++) {
                    int endLine = context.get(j + shift).getEnd().get().line;
                    statementSequence.add(context.get(j + shift));
                    newBlock = new BlockStmt(statementSequence);
                    methodDeclaration.setBody(newBlock);
                    Fragment sf = new Fragment(this, beginLine, endLine);
                    try {
                        sf.process(fw);
                    } catch (Exception e) {
                        String errMsg = String.format("Could not process statements \n%s\n", methodDeclaration);
                        logger.log(Level.ERROR, errMsg);
                    }
                }
                statementSequence.clear();
            }
        }
    }

    public class Fragment {
        private final List<ICSVItem> features;
        private final int beginLine;
        private final int endLine;
        private final String remainder;
        private final String candidate;
        private final Method parentMethod;
        private final Logger logger;
        private final MethodDeclaration methodDeclaration;
        private double score;

        public Fragment(Method method, int beginLine, int endLine) {
            this.features = new ArrayList<>();
            this.score = 0;
            this.parentMethod = method;
            this.methodDeclaration = method.methodDeclaration;
            int tmp = setLineBias();

            this.candidate = getCodeByLines(beginLine, endLine);
            this.beginLine = beginLine + tmp;
            this.endLine = endLine - tmp;
            this.logger = method.logger;
            this.remainder = remainderFromFile(methodBeginLine, methodEndLine, this.beginLine, this.endLine);
        }

        private final int setLineBias() {
            String unclearedCode = this.methodDeclaration.getBody().get().toString();
            int i = 0;
            int res = 0;
            while (unclearedCode.charAt(i) == '{' || isUselessChar(unclearedCode.charAt(i))) {
                if (unclearedCode.charAt(i) == '{') res++;
                i++;
            }
            return res - 1;
        }

        public final int getBeginLine() {
            return beginLine;
        }

        public final int getEndLine() {
            return endLine;
        }

        public final String getRemainder() {
            return remainder;
        }

        /**
         * computes remainder (method \ fragment) for a given fragment and sets corresponding field
         */
        private String remainderFromFile(int methodBegin, int methodEnd, int fragmentBegin, int fragmentEnd) {
            List<String> remainderLines = new ArrayList<>();
            try {
                List<String> lineStream = Files.readAllLines(Paths.get(repoName + "/" + filePath));
                boolean dummyCall = true;
                for (int i = methodBegin; i <= methodEnd; i++) {
                    if (i < fragmentBegin || i > fragmentEnd) {
                        remainderLines.add(lineStream.get(i - 1));
                    } else if (dummyCall) {
                        dummyCall = false;
                        remainderLines.add("\tcallToExtractedMethod();");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return clearCode(String.join("\n", remainderLines));
        }

        private void keywordFeaturesComputation() {
            try {
                KeywordsCalculator.extractToList(this.getCandidate(), this.features, countLines(this.candidate));
            } catch (Exception e) {
                logger.log(Level.ERROR, "Could not make keyword features' computation, repo: " + repoName);
            }
        }

        private void historicalFeaturesComputation() {
            try {
                GitBlameAnalyzer.extractToList(parentMethod.getRepository(), this.getBeginLine(), this.getEndLine(), parentMethod.getFilePath(), features);
            } catch (Exception e) {
                logger.log(Level.ERROR, "Could not make historical features' computation, repo: " + repoName);
            }
        }

        private void couplingFeaturesComputation() {
            try {
                Node node = methodDeclaration;
                while (node.getParentNode().isPresent())
                    node = node.getParentNode().get();
                Node root = node;
                MembersSets members = new MemberSetsGenerator().instanceMembers(root);
                int totalConnectivity = CouplingCalculator.calcConnectivity(getCandidate(), members.total);
                int methodConnectivity = CouplingCalculator.calcConnectivity(getCandidate(), members.methods);
                int fieldsConnectivity = CouplingCalculator.calcConnectivity(getCandidate(), members.fields);
                int lines = countLines(this.candidate);
                features.add(new CSVItem(Feature.FieldConnectivity, fieldsConnectivity));
                features.add(new CSVItem(Feature.FieldConnectivityPerLine, (double) fieldsConnectivity / lines));
                features.add(new CSVItem(Feature.TotalConnectivity, totalConnectivity));
                features.add(new CSVItem(Feature.TotalConnectivityPerLine, (double) totalConnectivity / lines));
                features.add(new CSVItem(Feature.MethodConnectivity, methodConnectivity));
                features.add(new CSVItem(Feature.MethodConnectivityPerLine, (double) methodConnectivity / lines));

            } catch (Exception e) {
                logger.log(Level.ERROR, "Could not make coupling features' computation, repo: " + repoName);
            }
        }

        private void methodDeclarationFeaturesComputation() {
            int methodDepth = parentMethod.getMethodDepth();
            int sequenceDepth = getNestingArea(this.getCandidate());

            features.add(new CSVItem(Feature.TotalLinesDepth, sequenceDepth));
            features.add(new CSVItem(Feature.AverageLinesDepth, (double) sequenceDepth / countLines(this.candidate)));
            features.add(new CSVItem(Feature.MethodDeclarationDepth, methodDepth));
            features.add(new CSVItem(Feature.MethodDeclarationDepthPerLine, (double) methodDepth / countLines(this.getMethod())));

            features.add(new CSVItem(Feature.MethodDeclarationSymbols, getMethod().length()));
            features.add(new CSVItem(Feature.MethodDeclarationAverageSymbols, (double) getMethod().length() /countLines(this.getMethod())));
        }

        private void lengthFeaturesComputation() {
            features.add(new CSVItem(Feature.TotalSymbolsInCodeFragment, getCandidate().length()));
            features.add(new CSVItem(Feature.AverageSymbolsInCodeLine, (double) getCandidate().length() / countLines(this.candidate)));
            features.add(new CSVItem(Feature.TotalLinesOfCode, countLines(this.candidate)));
        }

        private void rankingScoreComputation() {
            RankEvaluator ranker = new RankEvaluator(this.getCandidate(), this.getRemainder(), parentMethod.getMethodDepth(), parentMethod.getMethodArea());
            this.score = ranker.getScore();
            if ((this.score < 0))
                logger.log(Level.ERROR, "Unexpected score!");
        }

        /**
         * Returns body of the fragment (sequence of statements)
         * w/o leading and trailing curly braces, whitespaces, linebreaks
         */
        public String getCandidate() {
            return this.candidate;
        }

        /**
         * Returns string representation of the enclosing method
         */
        public String getMethod() {
            return this.parentMethod.getInitialMethod();
        }

        private void writeFeatures(FileWriter fw) throws IOException {
            int nFeatures = features.size();
            features.sort(Comparator.comparingInt(ICSVItem::getId));
            for (int i = 0; i < nFeatures; ++i) {
                fw.append(String.valueOf(features.get(i).getValue()));
                fw.append(';');
            }
            fw.append(String.valueOf(this.score));
            fw.append(";");
            fw.append("-\n");
        }

        public void process(FileWriter fw) throws IOException {
            keywordFeaturesComputation();
            historicalFeaturesComputation();
            couplingFeaturesComputation();
            methodDeclarationFeaturesComputation();
            lengthFeaturesComputation();

            rankingScoreComputation();

            writeFeatures(fw);
        }
    }

}