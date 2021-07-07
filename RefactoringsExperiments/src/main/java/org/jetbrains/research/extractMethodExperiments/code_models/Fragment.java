package org.jetbrains.research.extractMethodExperiments.code_models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.commons.lang3.StringUtils;
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

    /** repo from which the fragment was taken */
    private Repository repo;

    /** path to the file from which the fragment was taken */
    private String filePath;
    private List<Statement> statements;
    private String initialMethodStr;

    public Fragment(Node mDec, Repository repo, String filePath) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.repo = repo;
        this.filePath = filePath;
        this.statements = getSubStatements(methodDeclaration.getBody());
        this.initialMethodStr = methodDeclaration.toString();
    }

    private String clearCode(String code) {
        return code.replaceAll("^[ \t{\n]+|[ \t}\n]+$", "");
    }

    private class SubFragment {
        private MethodDeclaration methodDeclaration;
        /** repo from which the fragment was taken */
        private Repository repo;

        /** path to the file from which the fragment was taken */
        private String filePath;
        private List<ICSVItem> features;
        private String initialMethodStr;
        private int beginLine;
        private int endLine;
        private String complement;
        private double score;

        public SubFragment(Fragment fragment, int beginLine, int endLine) {
            this.methodDeclaration = fragment.methodDeclaration;
            this.repo = fragment.repo;
            this.filePath = fragment.filePath;
            this.features = new ArrayList<>();
            this.initialMethodStr = fragment.initialMethodStr;
            this.beginLine = beginLine;
            this.endLine = endLine;
            this.complement = makeComplement();
            this.score = 0;
        }

        private String makeComplement() {
            int relativeSubFragmentBeginLine = this.getBeginLine() - methodDeclaration.getBeginLine();
            int relativeSubFragmentEndLine = this.getEndLine() - methodDeclaration.getBeginLine();
            List<String> lines = Arrays.asList(initialMethodStr.split("\n"));
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
                System.out.printf("Method-------------\n%s\n-------------\n\n" +
                                "Fragment---------------\n%s\n---------------\n\n" +
                                "FragmentLines---------------\n%s\n---------------\n\n" +
                                "mBegin %d, mEnd %d, fBegin %d, fEnd %d", initialMethodStr,
                        this.getBody(), lines, methodDeclaration.getBeginLine(), methodDeclaration.getEndLine(),
                        this.getBeginLine(), this.getBeginLine());
            }
            return String.join("\n", complementLines);
        }

        private int getBeginLine() {
            return beginLine;
        }

        private int getEndLine() {
            return endLine;
        }

        private void keywordFeaturesComputation() {
            try {
                KeywordsCalculator.extractToList(this.getBody(), this.features, getBodyLineLength());
            } catch (Exception e) {
                System.err.println("Could not make keyword features' computation");
            }
        }

        private void historicalFeaturesComputation() {
            try {
                GitBlameAnalyzer.extractToList(repo, this.getBeginLine(), this.getEndLine(), filePath, features);
            } catch (Exception e) {
                System.err.println("Could not make historical features' computation");
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
                System.err.println("Could not make coupling features' computation");
                e.printStackTrace();
            }
        }

        private void methodDeclarationFeaturesComputation() {
            int methodDepth = analyzeDepth(this.getMethod());
            int sequenceDepth = analyzeDepth(this.getBody());

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

        private void rankingScoreComputation(double lengthSensitivity, double maxLengthScore) {
            RankEvaluater ranker = new RankEvaluater(this, lengthSensitivity, maxLengthScore);
            this.score = ranker.getScore();
        }

        int analyzeDepth(String code) {
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

        private int getBodyLineLength() {
            return (StringUtils.countMatches(getBody(), '\n') + 1);
        }

        private int getMethodLineLength() {
            return (StringUtils.countMatches(getMethod(), '\n') + 1);
        }

        /**
         * Returns body of the fragment (sequence of statements)
         * w/o leading and trailing curly braces, whitespaces, linebreaks
         */
        private String getBody() {
            return clearCode(this.methodDeclaration.getBody().toString());
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

        private void process(FileWriter fw) throws IOException {
            keywordFeaturesComputation();
            historicalFeaturesComputation();
            couplingFeaturesComputation();
            methodDeclarationFeaturesComputation();
            lengthFeaturesComputation();

            double lengthScoreSensitivity = 0.1;
            double maxLengthScore = 4;

            rankingScoreComputation(lengthScoreSensitivity, maxLengthScore);

            writeFeatures(fw);
        }

        protected class RankEvaluater {
            private String candidate;
            private String remainder;
            private String method;
            private double score;
            private int methodDepth;

            public RankEvaluater(SubFragment sf, double c_length, double max_length) {
                this.candidate = sf.getBody();
                this.method = sf.initialMethodStr;
                this.remainder = sf.complement;
                this.methodDepth = maxDepth(method);
                this.sNestArea();
                this.sLength(c_length, max_length);
                this.sNestDepth();
                this.sParam();
                this.sCommentsAndBlanks();
            }

            int maxDepth(String S) {
                int current_max = 0; // current count
                int max = 0; // overall maximum count
                int n = S.length();

                // Traverse the input string
                for (int i = 0; i < n; i++) {
                    if (S.charAt(i) == '{') {
                        current_max++;

                        // update max if required
                        if (current_max > max)
                            max = current_max;
                    } else if (S.charAt(i) == '}') {
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

            /**
             * Computation of Haas' nesting depth based score
             * `c` is a chosen coefficient, representing sensitivity of the score to length changes,
             * `max` is the upper bound on this part of the score
             */
            void sLength(double c, double max) {
                score += Math.min(c * Math.min(candidate.length(), remainder.length()), max);
            }

            /**
             * Computation of Haas' nesting depth based score
             */
            void sNestDepth() {
                int depthMethod = this.methodDepth;
                int depthRemainder = maxDepth(remainder);
                int depthCandidate = maxDepth(candidate);
                score += Math.min(depthMethod - depthRemainder, depthMethod - depthCandidate);

            }

            /**
             * Computation of Haas' nesting area based score,
             * 2 is stabilizing coefficient
             */
            void sNestArea() {
                int areaMethod = analyzeDepth(method);
                int areaRemainder = analyzeDepth(remainder);
                int areaCandidate = analyzeDepth(candidate);
                score += 2 * this.methodDepth / (double) areaMethod * Math.min(areaMethod - areaCandidate, areaMethod - areaRemainder);
            }

            void sParam() { //Placeholder for possible implementation of Haas' parameter-based score
                return;
            }

            void sCommentsAndBlanks() { //Placeholder for possible implementation of Haas' comments-based score
                return;
            }

            double getScore() {
                return this.score;
            }
        }

    }

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
                    statements.add((Statement) node);    //Here may be a problem of *chunky* statements
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
                        e.printStackTrace();
                        System.err.printf("Could not process subfragment \n%s\n.", methodDeclaration.toString());
                    }
                }
                statementSequence.clear();
            }
        }
    }
}