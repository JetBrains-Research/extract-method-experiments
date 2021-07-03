package ru.hse.kirilenko.refactorings.csv.models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import jdk.nashorn.internal.ir.Block;
import jdk.nashorn.internal.ir.BlockStatement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.lib.Repository;
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.utils.calcers.*;
import org.apache.commons.lang3.SerializationUtils;
import javax.swing.plaf.nimbus.State;
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

   /* private void keywordFeaturesComputation() {
        try {
            KeywordsCalculator.calculateToMap(this.getCode(), this.features, getLineLength(this.getCode()));
        } catch (Exception e) {
            System.err.println("Could not make keyword features' computation");
            e.printStackTrace();
        }
    }

    private void historicalFeaturesComputation() {
        try {
            GitBlameAnalyzer.extractFeaturesToMap(repo, methodDeclaration.getBeginLine(), methodDeclaration.getEndLine(), filePath, features);
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
            String code = getCode(); //To be replaced by sequence of statements
            int totalConnectivity = CouplingCalculator.calcConnectivity(getCode(), members.total);
            int methodConnectivity = CouplingCalculator.calcConnectivity(getCode(), members.methods);
            int fieldsConnectivity = CouplingCalculator.calcConnectivity(getCode(), members.fields);
            int lines = getLineLength(getCode());
            features.replace(Feature.FieldConnectivity, (double) fieldsConnectivity);
            features.replace(Feature.FieldConnectivityPerLine, (double) fieldsConnectivity / lines);
            features.replace(Feature.TotalConnectivity, (double) totalConnectivity);
            features.replace(Feature.TotalConnectivityPerLine, (double) totalConnectivity / lines);
            features.replace(Feature.MethodConnectivity, (double) methodConnectivity);
            features.replace(Feature.MethodConnectivityPerLine, (double) methodConnectivity / lines);

        } catch (Exception e) {
            System.err.println("Could not make coupling features' computation");
            e.printStackTrace();
        }
    }*/

    private void methodDeclarationFeaturesComputation() {
    }

    private static int getLineLength(String code) {
        return (StringUtils.countMatches(code, '\n') + 1);
    }

    private String getCode() {
        return this.methodDeclaration.toString();
    }

/*    private void computeFeatures() {
        keywordFeaturesComputation();
        historicalFeaturesComputation();
        couplingFeaturesComputation();
        methodDeclarationFeaturesComputation();
    }*/

    private void writeFeatures() {

    }

    public void processFragment(int threshold) {
//        System.out.printf("Method: ---------------\n%s\n", methodDeclaration.toString());
        BlockStmt block = methodDeclaration.getBody();
        List<Statement> statements = new ArrayList<>();
//        System.out.printf("Body: -----------------\n%s\n", block);
        for(Node node : block.getChildrenNodes()){
            if(node instanceof Statement){
//                System.out.printf("Statement: ------------\n%s\n", node);
                statements.add((Statement) node);    //Here may be a problem of *chunky* statements
            }
//            System.out.printf("Sub-statements: ------------\n%s\n",node.getChildrenNodes());
        }
        List<Statement> statementSequence = new ArrayList<>();
        if(statements.size() <= threshold){
            return;
        } else {
            for(int seqLength = threshold; seqLength<= statements.size(); seqLength++){
                System.out.printf("size %d, len %d \n", statements.size(), seqLength);
                for(int shift = 0; shift < statements.size() - seqLength; shift++){

                    for(int index = shift; index < seqLength + shift; index++) {
                        statementSequence.add(statements.get(index));
//                        System.out.printf("index %d, statement \n %s\n", index, statements.get(index));
                    }
                    //Shenanigans due to lack of copy-constructors and etc.

                    //Change body to compute stuff
                    BlockStmt newBlock = new BlockStmt(statementSequence);
                    methodDeclaration.setBody(newBlock);
                    System.out.printf("%d ---------------------\n%s\n", shift, methodDeclaration);
                    statementSequence.clear();
                }
            }

            //Shenanigans to restore original method.
//            methodDeclaration.setBody(block);
//            System.out.printf("Orig: -------------------\n%s\n", methodDeclaration);
            exit(0);
        }

    }

}