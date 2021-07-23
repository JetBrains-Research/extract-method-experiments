package org.jetbrains.research.extractMethodExperiments.haas;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;

import java.util.List;

/**
 * Candidate for extraction by Haas definition.
 */
public class Candidate {
    private final PsiMethod originalMethod;
    private final List<PsiStatement> statementList;
    private final String methodAsString;
    private String candidateAsString = "";
    private String remainderAsString = "";

    public Candidate(List<PsiStatement> statements, PsiMethod psiMethod) {
        this.originalMethod = psiMethod;
        this.statementList = statements;
        this.methodAsString = psiMethod.getText();
        calculateStrCandidate();
        calculateRemainder();
    }

    private void calculateRemainder() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(originalMethod.getProject(), () -> {
                for (PsiStatement statement : statementList) {
                    statement.getParent().deleteChildRange(statementList.get(0), statementList.get(statementList.size() - 1));
                }
                remainderAsString = originalMethod.getText();
            });
        });
    }

    public void calculateStrCandidate() {
        StringBuilder result = new StringBuilder();
        for (PsiStatement statement : this.statementList) {
            result.append(statement.getText());
            result.append('\n');
        }
        this.candidateAsString = result.toString();
    }

    public String getRemainderAsString() {
        return this.remainderAsString;
    }

    public String getMethodAsString() {
        return this.methodAsString;
    }

    public String getCandidateAsString() {
        return candidateAsString;
    }

    public PsiFile getContainingFile() {
        return originalMethod.getContainingFile();
    }

}
