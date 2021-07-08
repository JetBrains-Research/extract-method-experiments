package org.jetbrains.research.extractMethodExperiments.code_models;

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
import org.jetbrains.research.extractMethodExperiments.utils.calcers.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class Fragment {
    private MethodDeclaration methodDeclaration;
    /**
     * repo from which the fragment was taken
     */
    private Repository repo;

    /**
     * path to the file from which the fragment was taken
     */
    private String filePath;
    private List<Statement> statements;
    private String initialMethod;
    private Logger logger;
    private int methodDepth;
    private int methodArea;

    public Fragment(Node mDec, Repository repo, String filePath, Logger logger) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.repo = repo;
        this.filePath = filePath;
        this.statements = getSubStatements(methodDeclaration.getBody());
        this.initialMethod = methodDeclaration.toString();
        this.logger = logger;
        this.methodArea = getNestingArea(methodDeclaration.toString());
        this.methodDepth = getNestingDepth(methodDeclaration.toString());
    }

    public final String getInitialMethod(){
        return initialMethod;
    }

    public final int getMethodDepth(){
        return methodDepth;
    }

    public final int getMethodArea(){
        return methodArea;
    }

    public final Repository getRepository(){
        return repo;
    }

    public final String getFilePath(){
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
                    SubFragment sf = new SubFragment(this, beginLine, endLine, methodDepth, methodArea);
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

        public SubFragment(Fragment fragment, int beginLine, int endLine, int methodDepth, int methodArea) {
            this.features = new ArrayList<>();
            this.beginLine = beginLine;
            this.endLine = endLine;
            this.remainder = setRemainder();
            this.score = 0;
            this.parentFragment = fragment;
            this.methodDeclaration = fragment.methodDeclaration;
            this.logger = fragment.logger;
        }

        public final int getBeginLine() {
            return beginLine;
        }

        public final int getEndLine() {
            return endLine;
        }

        public final String getRemainder(){
            return remainder;
        }

        /**
         * computes remainder (method \ fragment) for a given subfragment and sets corresponding field
         * */
        private String setRemainder() {
            int relativeSubFragmentBeginLine = this.getBeginLine() - this.methodDeclaration.getBeginLine();
            int relativeSubFragmentEndLine = this.getEndLine() - this.methodDeclaration.getBeginLine();
            List<String> lines = Arrays.asList(this.parentFragment.getInitialMethod().split("\n"));
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
                        "Statements---------------\n%s\n---------------\n\n", this.parentFragment.getInitialMethod(), this.getBody());
                logger.log(Level.ERROR, errMsg);
            }
            return String.join("\n", complementLines);
        }

        private void keywordFeaturesComputation() {
            try {
                KeywordsCalculator.extractToList(this.getBody(), this.features, getBodyLineLength());
            } catch (Exception e) {
                logger.log(Level.ERROR, "Could not make keyword features' computation");
            }
        }

        private void historicalFeaturesComputation() {
            try {
                GitBlameAnalyzer.extractToList(parentFragment.getRepository(), this.getBeginLine(), this.getEndLine(), parentFragment.getFilePath(), features);
            } catch (Exception e) {
                logger.log(Level.ERROR,"Could not make historical features' computation");
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
                logger.log(Level.ERROR,"Could not make coupling features' computation");
            }
        }

        private void methodDeclarationFeaturesComputation() {
            int methodDepth = getNestingArea(this.getMethod());
            int sequenceDepth = getNestingArea(this.getBody());

            features.add(new CSVItem(Feature.TotalLinesDepth, (double) sequenceDepth));
            features.add(new CSVItem(Feature.AverageLinesDepth, (double) sequenceDepth / getBodyLineLength()));
            features.add(new CSVItem(Feature.MethodDeclarationDepth, (double) methodDepth));
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
            RankEvaluater ranker = new RankEvaluater(this, parentFragment.getInitialMethod(), parentFragment.getMethodArea(), parentFragment.getMethodDepth());
            this.score = ranker.getScore();
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

        private String getMethod() {
            return this.methodDeclaration.toString();
        }

        private void writeFeatures(FileWriter fw) throws IOException {
            int nFeatures = features.size();
            features.sort(Comparator.comparingInt(ICSVItem::getId));
            for (int i = 0; i < nFeatures; ++i) {
                fw.append(String.valueOf(features.get(i).getValue()));
                fw.append(';');
            }
            fw.append(String.valueOf(this.score)); //Score with 4 decimal places
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

    public static int getNestingArea(String code) {
        int dep = 0;
        int area = 0;
        int depInLine = 0;
        for (Character ch : code.toCharArray()) {
            if (ch == '{') {
                dep++;
                depInLine++;
            } else if (ch == '}') {
                dep--;
                depInLine--;
            } else if (ch == '\n') {
                int resDep = dep;
                if (depInLine > 0) {
                    resDep--;
                }
                depInLine = 0;
                area += resDep;
            }
        }
        return area;
    }

    public static int getNestingDepth(String str) {
        int current_max = 0; // current count
        int max = 0; // overall maximum count
        int n = str.length();

        // Traverse the input string
        for (int i = 0; i < n; i++) {
            if (str.charAt(i) == '{') {
                current_max++;

                // update max if required
                if (current_max > max)
                    max = current_max;
            } else if (str.charAt(i) == '}') {
                if (current_max > 0)
                    current_max--;
                else
                    return -1;
            }
        }

        // finally check for unbalanced string
        if (current_max != 0)
            return -1;

        return max;
    }

    public static String clearCode(String code) {
        return code.replaceAll("^[ \t{\n]+|[ \t}\n]+$", "");
    }

}