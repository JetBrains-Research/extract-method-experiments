package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HaasAlgorithm {
    private final PsiMethod originalMethod;
    private List<Candidate> candidateList;
    private final int minimumNumberOfStatements = 3;

    public HaasAlgorithm(PsiMethod psiMethod) {
        candidateList = new ArrayList<>();
        this.originalMethod = psiMethod;
        generateStatementSequencesInMethod(psiMethod);
    }

    /**
     * Generates sequences of statements that could be extracted into s separated method using Silva algorithm.
     */
    private void generateStatementSequencesInMethod(PsiMethod psiMethod) {
        if (psiMethod.getBody() != null) {
            @Nullable PsiCodeBlock methodBodyBlock = psiMethod.getBody();
            @NotNull Collection<PsiBlockStatement> blockStatements = PsiTreeUtil.findChildrenOfType(methodBodyBlock, PsiBlockStatement.class);

            for (PsiBlockStatement blockStatement : blockStatements) {
                PsiElement @NotNull [] innerStatements = blockStatement.getCodeBlock().getStatements();
                for (int i = 0; i <= innerStatements.length; i++) {
                    for (int j = i; j <= innerStatements.length; j++) {
                        Candidate candidate = calculateCandidate(blockStatement, i, j);
                        candidateList.add(candidate);
                    }
                }
            }
        }
    }

    private Candidate calculateCandidate(PsiStatement psiStatement, int i, int j) {
        List<PsiStatement> statementList = new ArrayList<>();
        @NotNull List<PsiStatement> bodyStatements = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiStatement, PsiStatement.class));
        for (int n = i; n < j; n++) {
            statementList.add(bodyStatements.get(n));
        }
        return new Candidate(statementList, originalMethod);
    }

    public List<Candidate> getCandidateList() {
        return candidateList;
    }

}
