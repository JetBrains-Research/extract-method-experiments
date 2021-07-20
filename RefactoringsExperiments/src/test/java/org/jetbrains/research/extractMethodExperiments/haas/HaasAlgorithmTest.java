package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the implementation of Haas algorithm:
 * - generation of candidates for extraction;
 * - TODO: calculation of remainders;
 * - TODO: calculation of candidate's metrics;
 * - TODO: candidates' ranking.
 */
public class HaasAlgorithmTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return LightJavaCodeInsightFixtureTestCase.JAVA_8;
    }

    @NotNull
    @Override
    protected String getTestDataPath() {
        return "src/test/testData/";
    }

    public void testCandidatesGeneration() {
        //Example from Silva's paper
        String code =
                "public class Test1 {" +
                        "   public void mouseReleased(MouseEvent me) {\n" +
                        "       for (Button btn : buttons) {\n" +
                        "           int cx = btn.fig.getX() + btn.fig.getWidth() - btn.icon.getIconWidth();\n" +
                        "           int cy = btn.fig.getY();\n" +
                        "           int cw = btn.icon.getIconWidth();\n" +
                        "           int ch = btn.icon.getIconHeight();\n" +
                        "           Rectangle rect = new Rectangle(cx, cy, cw, ch);\n" +

                        "           if (rect.contains(me.getX(), me.getY())) {\n" +
                        "               Object metaType = btn.metaType;\n" +
                        "               FigClassifierBox fcb = (FigClassifierBox) getContent();\n" +
                        "               FigCompartment fc = fcb.getCompartment(metaType);\n" +
                        "               fc.setEditOnRedraw(true);\n" +
                        "               fc.createModelElement();\n" +
                        "               me.consume();\n" +
                        "               return;\n" +
                        "           }\n" +
                        "       }\n" +
                        "   super.mouseReleased(me);\n" +
                        "   }" +
                        "}";

        PsiFile psiFile = myFixture.configureByText("Test1.java", code);
        PsiMethod psiMethod = PsiTreeUtil.findChildOfType(psiFile, PsiMethod.class);
        HaasAlgorithm algorithm = new HaasAlgorithm(psiMethod);
        List<Candidate> candidateList = algorithm.getCandidateList();
        assertNotEmpty(candidateList);
        //TODO: compare candidates the algorithm produces with the correct ones
    }

    public List<String> getAllStatementSeqsFromStatements(List<String> statements){
        List<String> result = new ArrayList<>();
        for(int size = 1; size != statements.size(); size++){
            for(int shift = 0; shift <= statements.size() - size; shift++) {
                String sequence = "";
                for(int i = shift; i < statements.size(); i++)
                    sequence += statements.get(i);
                result.add(sequence);
            }
        }
        return result;
    }

    public void generateTrueCandidates() {
        String c1 =
                "       for (Button btn : buttons) {\n" +
                "           int cx = btn.fig.getX() + btn.fig.getWidth() - btn.icon.getIconWidth();\n" +
                "           int cy = btn.fig.getY();\n" +
                "           int cw = btn.icon.getIconWidth();\n" +
                "           int ch = btn.icon.getIconHeight();\n" +
                "           Rectangle rect = new Rectangle(cx, cy, cw, ch);\n" +

                "           if (rect.contains(me.getX(), me.getY())) {\n" +
                "               Object metaType = btn.metaType;\n" +
                "               FigClassifierBox fcb = (FigClassifierBox) getContent();\n" +
                "               FigCompartment fc = fcb.getCompartment(metaType);\n" +
                "               fc.setEditOnRedraw(true);\n" +
                "               fc.createModelElement();\n" +
                "               me.consume();\n" +
                "               return;\n" +
                "           }\n" +
                "       }\n";

        String c1_1 =
                "           int cx = btn.fig.getX() + btn.fig.getWidth() - btn.icon.getIconWidth();\n";

        String c1_2 =
                "           int cx = btn.fig.getX() + btn.fig.getWidth() - btn.icon.getIconWidth();\n";

        String c1_3 =
                "           int cy = btn.fig.getY();\n";

        String c1_4 =
                "           int cw = btn.icon.getIconWidth();\n";

        String c1_5 =
                "           int ch = btn.icon.getIconHeight();\n";

        String c1_6 =
                "           Rectangle rect = new Rectangle(cx, cy, cw, ch);\n";

        String c1_7 =
                "           if (rect.contains(me.getX(), me.getY())) {\n" +
                "               Object metaType = btn.metaType;\n" +
                "               FigClassifierBox fcb = (FigClassifierBox) getContent();\n" +
                "               FigCompartment fc = fcb.getCompartment(metaType);\n" +
                "               fc.setEditOnRedraw(true);\n" +
                "               fc.createModelElement();\n" +
                "               me.consume();\n" +
                "               return;\n" +
                "           }\n";
        String c1_7_1 =
                "               Object metaType = btn.metaType;\n";

        String c1_7_2 =
                "               FigClassifierBox fcb = (FigClassifierBox) getContent();\n";

        String c1_7_3 =
                "               FigCompartment fc = fcb.getCompartment(metaType);\n";

        String c1_7_4 =
                "               fc.setEditOnRedraw(true);\n";

        String c1_7_5 =
                "               fc.createModelElement();\n";

        String c1_7_6 =
                "               me.consume();\n";

        String c1_7_7 =
                "               return;\n";
        List<String> statements0 = new ArrayList<>();
        statements0.add(c1);

        List<String> statements1 = new ArrayList<>();
        statements1.add(c1_1);
        statements1.add(c1_2);
        statements1.add(c1_3);
        statements1.add(c1_4);
        statements1.add(c1_5);
        statements1.add(c1_6);
        statements1.add(c1_7);

        List<String> statements2 = new ArrayList<>();
        statements2.add(c1_7_1);
        statements2.add(c1_7_2);
        statements2.add(c1_7_3);
        statements2.add(c1_7_4);
        statements2.add(c1_7_5);
        statements2.add(c1_7_6);
        statements2.add(c1_7_7);

        List<String> candidates1 = getAllStatementSeqsFromStatements(statements0);
        List<String> candidates2 = getAllStatementSeqsFromStatements(statements1);
        List<String> candidates3 = getAllStatementSeqsFromStatements(statements2);

        candidates1.addAll(candidates2);
        candidates1.addAll(candidates3);
    }
}
