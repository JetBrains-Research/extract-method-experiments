package org.jetbrains.research.extractMethodExperiments.models;

import org.apache.commons.lang3.StringUtils;

import static org.jetbrains.research.extractMethodExperiments.models.Fragment.getNestingArea;
import static org.jetbrains.research.extractMethodExperiments.models.Fragment.getNestingDepth;

public class RankEvaluator {
    private final double lengthScoreSensitivity = 0.1;
    private final double maxLengthScore = 3;
    private String candidate;
    private String remainder;
    private String method;
    private double score;
    private int methodDepth;
    private int methodArea;

    public RankEvaluator(Fragment.SubFragment sf, String initialMethod, int methodArea, int methodDepth) {
        this.candidate = sf.getBody();
        this.method = initialMethod;
        this.remainder = sf.getRemainder();
        this.methodDepth = methodDepth;
        this.methodArea = methodArea;

        this.setScore();
    }

    private void setScore() {
        score = sLength() + sNestArea() + sNestDepth() + sParam() + sCommentsAndBlanks();
    }

    /**
     * Computation of Haas' length based score
     * `c` is a chosen coefficient, representing sensitivity of the score to length changes,
     * `max` is the upper bound on this part of the score
     */
    double sLength() {
        int candidateLineLength = StringUtils.countMatches(candidate, '\n') + 1;
        int remainderLineLength = StringUtils.countMatches(remainder, '\n') + 1;

        return Math.min(lengthScoreSensitivity * Math.min(candidateLineLength, remainderLineLength), maxLengthScore);
    }

    /**
     * Computation of Haas' nesting depth based score
     */
    double sNestDepth() {
        int depthMethod = this.methodDepth;
        int depthRemainder = getNestingDepth(remainder);
        int depthCandidate = getNestingDepth(candidate);
        return Math.min(depthMethod - depthRemainder, depthMethod - depthCandidate);
    }

    /**
     * Computation of Haas' nesting area based score,
     * 2 is stabilizing coefficient
     */
    double sNestArea() {
        int areaMethod = this.methodArea;
        int areaRemainder = getNestingArea(remainder);
        int areaCandidate = getNestingArea(candidate);
        return 2 * this.methodDepth / (double) areaMethod * Math.min(areaMethod - areaCandidate, areaMethod - areaRemainder);
    }

    double sParam() { //Placeholder for possible implementation of Haas' parameter-based score
        return 0;
    }

    double sCommentsAndBlanks() { //Placeholder for possible implementation of Haas' comments-based score
        return 0;
    }

    double getScore() {
        return this.score;
    }
}