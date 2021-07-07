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

import static java.lang.System.exit;

public class Fragment {
    private MethodDeclaration methodDeclaration;
    private Repository repo;
    /**
     * repo from which the fragment was taken
     */
    private String filePath;
    /**
     * path to the file from which the fragment was taken
     */
    private String commitId;
    /**
     * commit from which the fragment was taken
     */

    private List<Statement> statements;
    private String initialMethodStr;

    public Fragment(Node mDec, Repository repo, String filePath, String commitId) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.commitId = commitId;
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
        private Repository repo;
        /**
         * repo from which the fragment was taken
         */
        private String filePath;
        /**
         * path to the file from which the fragment was taken
         */
        private String commitId;
        private List<ICSVItem> features;
        private String initialMethodStr;
        private int beginLine;
        private int endLine;
        private String complement;
        private double score;

        public SubFragment(Fragment fragment, int beginLine, int endLine) {
            this.methodDeclaration = fragment.methodDeclaration;
            this.commitId = fragment.commitId;
            this.repo = fragment.repo;
            this.filePath = fragment.filePath;
            this.features = new ArrayList<>();
            this.initialMethodStr = fragment.initialMethodStr;
            this.beginLine = beginLine;
            this.endLine = endLine;
            this.complement = makeComplement();
            this.score = 0;
//            System.out.printf("subfragment ------------------\n%s\n-------------------\n", this.getBody());
//            System.out.printf("complement ------------------\n%s\n-------------------\n", complement);

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
                exit(0);
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
//                e.printStackTrace();
            }
        }

        private void historicalFeaturesComputation() {
            try {
                GitBlameAnalyzer.extractToList(repo, this.getBeginLine(), this.getEndLine(), filePath, features);
            } catch (Exception e) {
                System.err.println("Could not make historical features' computation");
//                e.printStackTrace();
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

        private void rankingScoreComputation(double c_length, double max_length) {
            RankEvaluater ranker = new RankEvaluater(this, c_length, max_length);
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
            rankingScoreComputation(0.1, 4);

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

            void sLength(double c, double max) {
                score += Math.min(c * Math.min(candidate.length(), remainder.length()), max);
            }

            void sNestDepth() {
                int d_m = this.methodDepth;
                int d_r = maxDepth(remainder);
                int d_c = maxDepth(candidate);
                score += Math.min(d_m - d_r, d_m - d_c);

            }

            void sNestArea() {
                int a_m = analyzeDepth(method);
                int a_r = analyzeDepth(remainder);
                int a_c = analyzeDepth(candidate);
                score += 2 * this.methodDepth / (double) a_m * Math.min(a_m - a_c, a_m - a_r);
            }

            void sParam() {
                return;
            }

            void sCommentsAndBlanks() {
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
//        System.out.printf("Body: -----------------\n%s\n", block);
        for (Node node : s.getChildrenNodes()) {
            if (node instanceof Statement) {
//                System.out.printf("Statement: ------------\n%s\n", node);
                if (isValidStatement((Statement) node))
                    statements.add((Statement) node);    //Here may be a problem of *chunky* statements
            }
//            System.out.printf("Sub-statements: ------------\n%s\n",node.getChildrenNodes());
        }
        return statements;
    }

    public void processFragment(int threshold, FileWriter fw) {
        processStatement(threshold, this.statements, fw);
    }

    /**
     * Splits fragment into sequences of statements of length
     * greater than `threshold`, and processes each such
     * sequence, generating a row to the specified file
     */
    private void processStatement(int threshold, List<Statement> context, FileWriter fw) {
        // Cycle for parsing embeddings of statements
        for (Statement s : context) {
            if (!s.getChildrenNodes().isEmpty()) {
//                System.out.printf("Found an embedding!:\n%s\n%s\n", s.toString(), s.getChildrenNodes().toString());
                processStatement(threshold, getSubStatements(s), fw);
            }
//            System.out.printf("Sub-statements: ------------\n%s\n",node.getChildrenNodes());
        }
        List<Statement> statementSequence = new ArrayList<>();

        // Don't analyze too short methods
        if (context.size() < threshold) {
            return;
        } else {
//            System.out.printf("Method: ---------------\n%s\n", methodDeclaration.toString());
//            System.out.printf("begin %d, end %d\n", methodDeclaration.getBeginLine(), methodDeclaration.getEndLine());
            int seqCount = 0;
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
//                    System.out.printf("begin %d, end %d\n", beginLine, endLine);
                    SubFragment sf = new SubFragment(this, beginLine, endLine);
//                    System.out.printf("Row #%d----------------------------\n%s\n", seqCount, methodDeclaration.toString());
                    try {
                        sf.process(fw);
//                        System.out.printf("begin %d, end %d\n", sf.methodDeclaration.getBeginLine(), sf.methodDeclaration.getEndLine())
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.printf("Could not process subfragment \n%s\n.", methodDeclaration.toString());
                    }

                    seqCount++;
                }
                statementSequence.clear();
            }
            //Shenanigans due to lack of copy-constructors and etc.

            //Change body to compute stuff

            //Shenanigans to restore the original method.
//            methodDeclaration.setBody(block);
//            System.out.printf("Orig: -------------------\n%s\n", methodDeclaration);
        }
    }
}