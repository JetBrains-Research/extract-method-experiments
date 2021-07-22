package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HaasAlgorithm {
    private List<Candidate> candidateList;
    private final int minimumNumberOfStatements = 3;

    public HaasAlgorithm(PsiMethod psiMethod) {
        candidateList = new ArrayList<>();
        generateStatementSequencesInMethod(psiMethod);
    }

    /**
     * Generates sequences of statements that could be extracted into s separated method using Silva algorithm.
     * Processes statements by their depth level and generates candidates only within the same depth level.
     */
    private void generateStatementSequencesInMethod(PsiMethod psiMethod) {
        if (psiMethod.getBody() != null) {
            @Nullable PsiCodeBlock methodBodyBlock = psiMethod.getBody();
            generateStatementsWithinOneBlock(methodBodyBlock, psiMethod);
            @NotNull Collection<PsiBlockStatement> blockStatements = PsiTreeUtil.findChildrenOfType(methodBodyBlock, PsiBlockStatement.class);
            blockStatements.forEach(s -> generateStatementsWithinOneBlock(s.getCodeBlock(), psiMethod));
        }
    }

    private void generateStatementsWithinOneBlock(PsiCodeBlock codeBlock, PsiMethod psiMethod) {
        PsiStatement[] psiStatements = codeBlock.getStatements();
        //TODO: take into account minimumNumberOfStatements
        for (int i = 0; i < psiStatements.length; i++) {
            for (int j = i; j < psiStatements.length; j++) {
                Candidate candidate = calculateCandidate(psiMethod, codeBlock, i, j);
                candidateList.add(candidate);
            }
        }
    }

    private Candidate calculateCandidate(PsiMethod psiMethod, PsiCodeBlock codeBlock, int i, int j) {
        PsiCodeBlock copyBlock = (PsiCodeBlock) codeBlock.copy();
        List<PsiStatement> statementList = new ArrayList<>(Arrays.asList(copyBlock.getStatements()).subList(i, j));
        return statementList.size() > 0 ? new Candidate(statementList, psiMethod) : null;
    }

    public List<Candidate> getCandidateList() {
        return candidateList;
    }

}
