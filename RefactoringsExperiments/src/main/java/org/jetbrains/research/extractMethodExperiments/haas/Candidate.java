package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;

import java.util.List;

/**
 * Candidate for extraction by Haas definition.
 */
public class Candidate {
    private final PsiMethod originalMethod;
    private final List<PsiStatement> statementList;
    private PsiMethod remainder;
    private double length;

    public Candidate(List<PsiStatement> statements, PsiMethod psiMethod) {
        this.originalMethod = psiMethod;
        this.statementList = statements;
        calculateRemainder();
    }

    private void calculateRemainder() {
        PsiMethod methodAfterRemoving = originalMethod;
        //TODO: debug it and fix
        // methodAfterRemoving.deleteChildRange(statementList.get(0), statementList.get(statementList.size()));
        this.remainder = methodAfterRemoving;
    }

    public List<PsiStatement> getStatementList() {
        return this.statementList;
    }

    public PsiMethod getRemainder() {
        return remainder;
    }
}
