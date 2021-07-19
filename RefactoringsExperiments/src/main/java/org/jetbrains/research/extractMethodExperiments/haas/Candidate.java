package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Candidate for extraction by Haas definition.
 */
public class Candidate {
    private final PsiMethod originalMethod;
    private final List<PsiStatement> statementList;
    private final List<PsiStatement> remainedStatements;

    public Candidate(List<PsiStatement> statements, PsiMethod psiMethod) {
        this.originalMethod = psiMethod;
        this.statementList = statements;
        this.remainedStatements = new ArrayList<>();
        calculateRemainder();
    }

    private void calculateRemainder() {
        Collection<PsiStatement> childrenOfType = PsiTreeUtil.findChildrenOfType(originalMethod, PsiStatement.class);
        for (PsiStatement statement : childrenOfType) {
            if (!statementList.contains(statement)) {
                remainedStatements.add(statement);
            }
        }
    }

    public List<PsiStatement> getStatementList() {
        return this.statementList;
    }


    public List<PsiStatement> getRemainedStatements() {
        return remainedStatements;
    }

}
