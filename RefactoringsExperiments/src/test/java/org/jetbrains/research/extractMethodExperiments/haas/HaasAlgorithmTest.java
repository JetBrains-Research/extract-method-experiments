package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

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

    public void checkCountOfGeneratedCandidates() {
        String code =
                "public class Test1 {\n" +
                        "    public void test1() {\n" +
                        "        int a = 1;\n" +
                        "        int b = 2;\n" +
                        "        int c = 3;\n" +
                        "        int d = 4;\n" +
                        "        int e = 5;\n" +
                        "    }\n" +
                        "}";
        PsiFile psiFile = myFixture.configureByText("Test1.java", code);
        PsiMethod psiMethod = PsiTreeUtil.findChildOfType(psiFile, PsiMethod.class);
        HaasAlgorithm algorithm = new HaasAlgorithm(psiMethod);
        List<Candidate> candidateList = algorithm.getCandidateList();
        assertEquals(15, candidateList.size());
    }

    public void testCandidatesGeneration2() {
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
}
