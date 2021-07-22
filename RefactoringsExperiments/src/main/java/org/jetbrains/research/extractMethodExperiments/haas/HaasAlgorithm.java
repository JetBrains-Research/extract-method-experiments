package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
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
     */
    private void generateStatementSequencesInMethod(PsiMethod psiMethod) {
        if (psiMethod.getBody() != null) {
            @Nullable PsiCodeBlock methodBodyBlock = psiMethod.getBody();
            PsiStatement[] psiStatements = methodBodyBlock.getStatements();
            //TODO: take into account minimumNumberOfStatements
            for (int i = 0; i < psiStatements.length; i++) {
                for (int j = i; j < psiStatements.length; j++) {
                    Candidate candidate = calculateCandidate(psiMethod, i, j);
                    candidateList.add(candidate);
                }
            }
        }
    }

    private Candidate calculateCandidate(PsiMethod method, int i, int j) {
        PsiMethod copyMethod = (PsiMethod) method.copy();
        List<PsiStatement> statementList = new ArrayList<>(Arrays.asList(copyMethod.getBody().getStatements()).subList(i, j + 1));
        return statementList.size() > 0 ? new Candidate(statementList, copyMethod) : null;
    }

    public List<Candidate> getCandidateList() {
        return candidateList;
    }

}
