package ru.hse.kirilenko.refactorings.csv.models;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jgit.lib.Repository;
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.utils.calcers.*;

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

    class Statement {
        String code;
        Map<Feature, Double> features;

        public Statement(String code) {
            this.code = code;
            this.features = new HashMap<Feature, Double>();
        }
    }

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

    public void processFragment() {

        ASTParser parser = ASTParser.newParser(JLS11);
        parser.setSource(methodDeclaration.toString().toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        Block block = (Block) parser.createAST(null);
        for(Object s : block.statements()){
            System.out.println(s.toString());
        }
    }

}