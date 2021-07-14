package org.jetbrains.research.extractMethodExperiments.models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.research.extractMethodExperiments.csv.models.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.csv.models.ICSVItem;
import org.jetbrains.research.extractMethodExperiments.utils.feature.generators.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.jetbrains.research.extractMethodExperiments.utils.feature.generators.DepthAnalyzer.getNestingArea;
import static org.jetbrains.research.extractMethodExperiments.utils.feature.generators.DepthAnalyzer.getNestingDepth;

public class Fragment {
    private MethodDeclaration methodDeclaration;
    /**
     * repo from which the fragment was taken
     */
    private Repository repo;
    private String repoName;
    /**
     * path to the file from which the fragment was taken
     */
    private String filePath;
    private List<Statement> statements;
    private String initialMethod;
    private Logger logger;
    private int methodDepth;
    private int methodArea;

    public Fragment(Node mDec, Repository repo, String repoName, String filePath, Logger logger) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.repo = repo;
        this.repoName = repoName;
        this.filePath = filePath;
        this.statements = getSubStatements(methodDeclaration.getBody());
        this.initialMethod = methodDeclaration.toString();
        this.logger = logger;
        this.methodArea = getNestingArea(methodDeclaration.toString());
        this.methodDepth = getNestingDepth(methodDeclaration.toString());
    }


    public static boolean isUselessChar(char c) {
        return (c == ' ' || c == '\t' || c == '\n');
    }

    /**
     * Clears the passed code from redundant curly braces and trims end-lines, tabs and whitespaces.
     */
    public static String clearCode(String code) {
        int trailingBracesToRemove = 0;
        char[] charArray = code.toCharArray();
        int index = 0;
        while (isUselessChar(charArray[index]) || charArray[index] == '{') {
            if (charArray[index] == '{') trailingBracesToRemove++;
            charArray[index] = ' ';
            index++;
        }
        index = charArray.length - 1;
        while (trailingBracesToRemove > 0) {
            if (charArray[index] == '}') {
                charArray[index] = ' ';
                trailingBracesToRemove--;
            }
            index--;
        }
        return String.valueOf(charArray).replaceAll("^[ \t\n]+|[ \t\n]+$", "");
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
     * Strips preceding and trailing curly braces, whitespaces and end-lines
     */

    private boolean isValidStatement(Statement s) {
        String linearText = clearCode(s.toString());
        linearText = linearText.replace("\n", "").replace("\r", "");
        return Pattern.matches(".*}", linearText) | Pattern.matches(".*;", linearText);
    }

    private List<Statement> getSubStatements(Statement s) {
        List<Statement> statements = new ArrayList<>();
        for (Node node : s.getChildrenNodes()) {
            if (node instanceof Statement) {
                if (isValidStatement((Statement) node))
                    statements.add((Statement) node);
            }
        }
        return statements;
    }

    public void processFragment(int threshold, FileWriter fw) {
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
            if (!s.getChildrenNodes().isEmpty()) {
                processStatements(threshold, getSubStatements(s), fw);
            }
        }
        List<Statement> statementSequence = new ArrayList<>();

        // Don't analyze too short methods
        if (context.size() >= threshold) {
            BlockStmt newBlock;
            for (int shift = 0; shift <= context.size() - threshold; shift++) {
                int beginLine = context.get(shift).getBeginLine();

                for (int i = 0; i < threshold - 1; i++) {
                    statementSequence.add(context.get(i + shift));
                }
                for (int j = threshold - 1; j + shift < context.size(); j++) {
                    int endLine = context.get(j + shift).getEndLine();
                    statementSequence.add(context.get(j + shift));
                    newBlock = new BlockStmt(statementSequence);

                    methodDeclaration.setBody(newBlock);
                    SubFragment sf = new SubFragment(this, beginLine, endLine);
                    try {
                        sf.process(fw);
                    } catch (Exception e) {
                        String errMsg = String.format("Could not process statements \n%s\n", methodDeclaration.toString());
                        logger.log(Level.ERROR, errMsg);
                    }
                }
                statementSequence.clear();
            }
        }
    }

    public class SubFragment {
        private List<ICSVItem> features;
        private int beginLine;
        private int endLine;
        private String remainder;
        private double score;
        private Fragment parentFragment;
        private Logger logger;
        private MethodDeclaration methodDeclaration;

        public SubFragment(Fragment fragment, int beginLine, int endLine) {
            this.features = new ArrayList<>();
            this.score = 0;
            this.parentFragment = fragment;
            this.methodDeclaration = fragment.methodDeclaration;
            int tmp = setLineBias();
            this.beginLine = beginLine + tmp - 1;
            this.endLine = endLine - tmp - 1;
            this.logger = fragment.logger;
            this.remainder = setRemainder();
        }

        private final int setLineBias() {
            String unclearedCode = this.methodDeclaration.getBody().toString();
            int i = 0;
            int res = 0;
            while (unclearedCode.charAt(i) == '{' || unclearedCode.charAt(i) == ' ' || unclearedCode.charAt(i) == '\t' || unclearedCode.charAt(i) == '\n') {
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
         * computes remainder (method \ fragment) for a given subfragment and sets corresponding field
         */
        private String setRemainder() {
            int relativeSubFragmentBeginLine = this.getBeginLine() - this.methodDeclaration.getBeginLine();
            int relativeSubFragmentEndLine = this.getEndLine() - this.methodDeclaration.getBeginLine();
            List<String> lines = Arrays.asList(this.parentFragment.methodDeclaration.getBody().toString().split("\n"));
            List<String> complementLines = new ArrayList<>();
            boolean dummyCall = true;
            try {
                for (int i = 0; i < lines.size(); i++) {
                    if ((relativeSubFragmentBeginLine > i) || (i > relativeSubFragmentEndLine))
                        complementLines.add(lines.get(i));
                    else if (dummyCall) {
                        complementLines.add("callToExtractedMethod();");
                        dummyCall = false;
                    }
                }
            } catch (Exception e) {
                String errMsg = String.format("Could not make a complement, details:\nMethod-------------\n%s\n-------------\n\n" +
                        "Statements---------------\n%s\n---------------\n\n", this.getMethod(), this.getBody());
                logger.log(Level.ERROR, errMsg);
            }
            return String.join("\n", complementLines);
        }

        private void keywordFeaturesComputation() {
            try {
                KeywordsCalculator.extractToList(this.getBody(), this.features, getBodyLineLength());
            } catch (Exception e) {
                logger.log(Level.ERROR, "Could not make keyword features' computation, repo: " + repoName);
            }
        }

        private void historicalFeaturesComputation() {
            try {
                GitBlameAnalyzer.extractToList(parentFragment.getRepository(), this.getBeginLine(), this.getEndLine(), parentFragment.getFilePath(), features);
            } catch (Exception e) {
                logger.log(Level.ERROR, "Could not make historical features' computation, repo: " + repoName);
            }
        }

        private void couplingFeaturesComputation() {
            try {
                Node node = methodDeclaration;
                while (node.getParentNode() != null)
                    node = node.getParentNode();
                Node root = node;
                MembersSets members = new MemberSetsGenerator().instanceMembers(root);
                int totalConnectivity = CouplingCalculator.calcConnectivity(getBody(), members.total);
                int methodConnectivity = CouplingCalculator.calcConnectivity(getBody(), members.methods);
                int fieldsConnectivity = CouplingCalculator.calcConnectivity(getBody(), members.fields);
                int lines = getBodyLineLength();
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
            int methodDepth = parentFragment.getMethodDepth();
            int sequenceDepth = getNestingArea(this.getBody());

            features.add(new CSVItem(Feature.TotalLinesDepth, sequenceDepth));
            features.add(new CSVItem(Feature.AverageLinesDepth, (double) sequenceDepth / getBodyLineLength()));
            features.add(new CSVItem(Feature.MethodDeclarationDepth, methodDepth));
            features.add(new CSVItem(Feature.MethodDeclarationDepthPerLine, (double) methodDepth / getMethodLineLength()));

            features.add(new CSVItem(Feature.MethodDeclarationSymbols, getMethod().length()));
            features.add(new CSVItem(Feature.MethodDeclarationAverageSymbols, (double) getMethod().length() / getMethodLineLength()));
        }

        private void lengthFeaturesComputation() {
            features.add(new CSVItem(Feature.TotalSymbolsInCodeFragment, getBody().length()));
            features.add(new CSVItem(Feature.AverageSymbolsInCodeLine, (double) getBody().length() / getBodyLineLength()));
            features.add(new CSVItem(Feature.TotalLinesOfCode, getBodyLineLength()));
        }

        private void rankingScoreComputation() {
            RankEvaluator ranker = new RankEvaluator(this.getBody(), this.remainder, parentFragment.getMethodDepth(), parentFragment.getMethodArea());
            this.score = ranker.getScore();
            if (!(this.score >= 0))
                logger.log(Level.ERROR, "Unexpected score!");
        }

        public int getBodyLineLength() {
            return (StringUtils.countMatches(getBody(), '\n') + 1);
        }

        public int getMethodLineLength() {
            return (StringUtils.countMatches(getMethod(), '\n') + 1);
        }

        /**
         * Returns body of the fragment (sequence of statements)
         * w/o leading and trailing curly braces, whitespaces, linebreaks
         */
        public String getBody() {
            return clearCode(methodDeclaration.getBody().toString());
        }

        /**
         * Returns string representation of the enclosing method
         */
        public String getMethod() {
            return this.parentFragment.initialMethod;
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