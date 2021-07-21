package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.psi.PsiElement;
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

    private final String strCandidate;
    private final String strRemainder;

    public Candidate(List<PsiStatement> statements, PsiMethod psiMethod) {
        this.originalMethod = psiMethod;
        this.statementList = statements;
        this.remainedStatements = new ArrayList<>();
        this.strCandidate = statementsToString(statementList);

        calculateRemainder();

        this.strRemainder = statementsToString(remainedStatements);
    }

    private static String statementsToString(List<PsiStatement> statements) {
        StringBuilder result = new StringBuilder();
        for (PsiStatement statement : statements) {
            result.append(statement.getText());
            result.append('\n');
        }
        return result.toString();
    }

    private void calculateRemainder() {
        Collection<PsiStatement> childrenOfType = PsiTreeUtil.findChildrenOfType(originalMethod, PsiStatement.class);
        for (PsiStatement statement : childrenOfType) {
            if (!statementList.contains(statement)) {
                PsiElement parent = statementList.get(0).getParent().getParent().getParent();
                if (!statement.equals(parent)) {
                    remainedStatements.add(statement);
                }
            }
        }
    }

    public List<PsiStatement> getStatementList() {
        return this.statementList;
    }


    public List<PsiStatement> getRemainedStatements() {
        return this.remainedStatements;
    }

    public String getStrRemainder() {
        return this.strRemainder;
    }

    public String getStrCandidate() {
        return this.strCandidate;
    }


}
