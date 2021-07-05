package ru.hse.kirilenko.refactorings.csv.models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.sun.tools.jdeprscan.CSV;
import jdk.nashorn.internal.ir.Block;
import jdk.nashorn.internal.ir.BlockStatement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.lib.Repository;
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.extractors.ExtractionConfig;
import ru.hse.kirilenko.refactorings.utils.calcers.*;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.System.exit;
import static org.eclipse.jdt.core.dom.AST.JLS10;
import static org.eclipse.jdt.core.dom.AST.JLS11;

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

    public Fragment(Node mDec, Repository repo, String filePath, String commitId) {
        this.methodDeclaration = (MethodDeclaration) mDec;
        this.commitId = commitId;
        this.repo = repo;
        this.filePath = filePath;
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
                e.printStackTrace();
            }
        }

        private void historicalFeaturesComputation() {
            try {
                GitBlameAnalyzer.extractToList(repo, methodDeclaration.getBody().getBeginLine(), methodDeclaration.getBody().getEndLine(), filePath, features);
            } catch (Exception e) {
                System.err.println("Could not make historical features' computation");
                e.printStackTrace();
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
    public void processFragment(int threshold, FileWriter fw) {
        BlockStmt block = methodDeclaration.getBody();
        List<Statement> statements = new ArrayList<>();
//        System.out.printf("Body: -----------------\n%s\n", block);
        for (Node node : block.getChildrenNodes()) {
            if (node instanceof Statement) {
//                System.out.printf("Statement: ------------\n%s\n", node);
                statements.add((Statement) node);    //Here may be a problem of *chunky* statements
            }
//            System.out.printf("Sub-statements: ------------\n%s\n",node.getChildrenNodes());
        }
        List<Statement> statementSequence = new ArrayList<>();

        // Don't analyze too short methods
        if (statements.size() < threshold) {
            return;
        } else {
//            System.out.printf("Method: ---------------\n%s\n", methodDeclaration.toString());
            int seqCount = 0;
            BlockStmt newBlock;
            for (int shift = 0; shift < statements.size() - threshold; shift++) {
                for (int i = 0; i < threshold - 1; i++) {
                    statementSequence.add(statements.get(i + shift));
                }
                for (int j = threshold - 1; j + shift < statements.size(); j++) {
                    statementSequence.add(statements.get(j + shift));
                    newBlock = new BlockStmt(statementSequence);
                    methodDeclaration.setBody(newBlock); //Here should be a call to feature computation
                    SubFragment sf = new SubFragment(this);
//                    System.out.printf("Row #%d----------------------------\n%s\n", seqCount, methodDeclaration.toString());
                    try {
                        sf.process(fw);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Could not process subfragment.");
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