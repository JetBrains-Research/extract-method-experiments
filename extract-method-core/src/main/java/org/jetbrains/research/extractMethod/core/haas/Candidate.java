package org.jetbrains.research.extractMethod.core.haas;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.jetbrains.research.extractMethod.core.utils.StatementListUtil.listToString;

/**
 * Candidate for extraction by Haas definition.
 */
public class Candidate implements Comparable<Candidate> {
    private final PsiMethod originalMethod;
    private final List<PsiStatement> statementList;
    private final String methodAsString;
    private final double maxLengthScore = 3;
    private final double lengthScoreSensitivity = 0.1;
    private final String candidateAsString;
    private String remainderAsString = "";
    private final int methodArea;
    private final int methodDepth;
    private double score;

    public Candidate(List<PsiStatement> statements, PsiMethod psiMethod) {
        this.originalMethod = psiMethod;
        this.statementList = statements;
        this.methodAsString = psiMethod.getText();
        this.candidateAsString = listToString(this.statementList);

        calculateRemainder();
        this.methodArea = getNestingArea(methodAsString);
        this.methodDepth = getNestingDepth(methodAsString);
        calculateScore();
    }

    /**
     * Calculates the remainder of the original method after extraction of statements from statementsList.
     */
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

    /**
     * Calculates the candidate's score by Haas definition.
     */
    private void calculateScore() {
        score = computeLength() + computeNestingArea() + computeNestingDepth() + getParametersCount() + getCommentsCount();
    }

    /**
     * Computation of Haas' length based score
     * `c` is a chosen coefficient, representing sensitivity of the score to length changes,
     * `max` is the upper bound on this part of the score
     */
    public double computeLength() {
        int candidateLineLength = StringUtils.countMatches(candidateAsString, '\n') + 1;
        int remainderLineLength = StringUtils.countMatches(remainderAsString, '\n') + 1;

        return Math.min(lengthScoreSensitivity * Math.min(candidateLineLength, remainderLineLength), maxLengthScore);
    }

    /**
     * Computation of Haas' nesting depth based score
     */
    public double computeNestingDepth() {
        int depthMethod = this.methodDepth;
        int depthRemainder = getNestingDepth(remainderAsString);
        int depthCandidate = getNestingDepth(candidateAsString);
        return Math.min(depthMethod - depthRemainder, depthMethod - depthCandidate);
    }

    /**
     * Computation of Haas' nesting area based score,
     * 2 is stabilizing coefficient
     */
    public double computeNestingArea() {
        int areaMethod = this.methodArea;
        int areaRemainder = getNestingArea(remainderAsString);
        int areaCandidate = getNestingArea(candidateAsString);
        return 2 * this.methodDepth / (double) areaMethod * Math.min(areaMethod - areaCandidate, areaMethod - areaRemainder);
    }

    public double getParametersCount() { //Placeholder for possible implementation of Haas' parameter-based score
        return 0;
    }

    double getCommentsCount() { //Placeholder for possible implementation of Haas' comments-based score
        return 0;
    }

    public double getScore() {
        return this.score;
    }

    public String getRemainderAsString() {
        return this.remainderAsString;
    }

    public String getMethodAsString() {
        return this.methodAsString;
    }

    public String getCandidateAsString() {
        return this.candidateAsString;
    }

    public final List<PsiStatement> getStatementList() {
        return this.statementList;
    }

    @Override
    public int compareTo(@NotNull Candidate o) {
        return Double.compare(this.getScore(), o.getScore());
    }
}
