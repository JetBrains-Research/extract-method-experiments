package ru.hse.kirilenko.refactorings.models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.Repository;
import ru.hse.kirilenko.refactorings.csv.models.CSVItem;
import ru.hse.kirilenko.refactorings.csv.models.Feature;
import ru.hse.kirilenko.refactorings.csv.models.ICSVItem;
import ru.hse.kirilenko.refactorings.utils.calcers.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.System.exit;

public class Fragment {
    MethodDeclaration methodDeclaration;
    Repository repo;
    /**
     * repo from which the fragment was taken
     */
    String filePath;
    /**
     * path to the file from which the fragment was taken
     */
    String commitId;

    /**
     * commit from which the fragment was taken
     */
    List<Statement> statements;
    public Fragment(Node mDec, Repository repo, String filePath, String commitId) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.commitId = commitId;
        this.repo = repo;
        this.filePath = filePath;
        this.statements = getSubStatements(methodDeclaration.getBody());
    }

    private class SubFragment {
        MethodDeclaration methodDeclaration;
        Repository repo;
        /**
         * repo from which the fragment was taken
         */
        String filePath;
        /**
         * path to the file from which the fragment was taken
         */
        String commitId;
        List<ICSVItem> features;


        public SubFragment(Fragment fragment) {
            this.methodDeclaration = fragment.methodDeclaration;
            this.commitId = fragment.commitId;
            this.repo = fragment.repo;
            this.filePath = fragment.filePath;
            this.features = new ArrayList<>();

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
                GitBlameAnalyzer.extractToList(repo, methodDeclaration.getBody().getBeginLine(), methodDeclaration.getBody().getEndLine(), filePath, features);
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

        private void lengthFeaturesComputation(){
            features.add(new CSVItem(Feature.TotalSymbolsInCodeFragment, getBody().length()));
            features.add(new CSVItem(Feature.AverageSymbolsInCodeLine, (double)getBody().length() /getBodyLineLength()));
            features.add(new CSVItem(Feature.TotalLinesOfCode, getBodyLineLength()));
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


        private String getBody() {
            return this.methodDeclaration.getBody().toString();
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
            fw.append("0\n");
        }

        private void process(FileWriter fw) throws IOException {
            keywordFeaturesComputation();
            historicalFeaturesComputation();
            couplingFeaturesComputation();
            methodDeclarationFeaturesComputation();
            lengthFeaturesComputation();

            writeFeatures(fw);
        }

    }


    /**
     * Splits fragment into sequences of statements of length
     * greater than `threshold`, and processes each such
     * sequence, generating a row to the specified file
     */

    private boolean isValidStatement(Statement s){
        String linearText = s.toString();
        linearText = linearText.replace("\n", "").replace("\r", "");
//        System.out.println(s.toString()+"\n-----------------------------------\n");
        if(Pattern.matches(".*}", linearText) | Pattern.matches(".*;", linearText))
            return true;
        else
            return false;
    }

    private List<Statement> getSubStatements(Statement s){
        List<Statement> statements = new ArrayList<>();
//        System.out.printf("Body: -----------------\n%s\n", block);
        for (Node node : s.getChildrenNodes()) {
            if (node instanceof Statement) {
//                System.out.printf("Statement: ------------\n%s\n", node);
                if(isValidStatement((Statement) node))
                    statements.add((Statement) node);    //Here may be a problem of *chunky* statements
            }
//            System.out.printf("Sub-statements: ------------\n%s\n",node.getChildrenNodes());
        }
        return statements;
    }

    public void processFragment(int threshold, FileWriter fw) {
        processEmbedding(threshold, methodDeclaration.getBody(), this.statements, fw);
        exit(0);
    }
    private void processEmbedding(int threshold, Statement embedding, List<Statement> context, FileWriter fw) {
        // Cycle for parsing embeddings of statements
        for (Statement s : context) {
            if (!s.getChildrenNodes().isEmpty()) {
//                System.out.printf("Found an embedding!:\n%s\n%s\n", s.toString(), s.getChildrenNodes().toString());
                processEmbedding(threshold, s, getSubStatements(s), fw);
            }
//            System.out.printf("Sub-statements: ------------\n%s\n",node.getChildrenNodes());
        }
        List<Statement> statementSequence = new ArrayList<>();

        // Don't analyze too short methods
        if (context.size() < threshold) {
            return;
        } else {
//            System.out.printf("Method: ---------------\n%s\n", methodDeclaration.toString());
            int seqCount = 0;
            BlockStmt newBlock;
            for (int shift = 0; shift <= context.size() - threshold; shift++) {
                for (int i = 0; i < threshold - 1; i++) {
                    statementSequence.add(context.get(i + shift));
                }
                for (int j = threshold - 1; j + shift < context.size(); j++) {

                    statementSequence.add(context.get(j + shift));
                    newBlock = new BlockStmt(statementSequence);
                    methodDeclaration.setBody(newBlock);
                    SubFragment sf = new SubFragment(this);
                    System.out.printf("Row #%d----------------------------\n%s\n", seqCount, methodDeclaration.toString());
                    try {
//                        sf.process(fw);
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