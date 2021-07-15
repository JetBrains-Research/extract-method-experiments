package org.jetbrains.research.extractMethodExperiments.utils.feature.generators;

/**
 * Utility class for computing depth-metrics of passed code
 */
public class DepthAnalyzer {
    /**
     * Generates and returns array of integers, representing the nesting level of each line
     */
    public static int[] getNestingDepths(String code) {
        String[] lines = code.split("\n");
        int[] depthInLine = new int[lines.length];
        int currentDepth = 0; // current count

        // Traverse the input strings
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("[{].*[}]", " ");
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == '{') break;

                if (line.charAt(j) == '}') {
                    currentDepth--;
                }
            }
            depthInLine[i] = currentDepth;
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == '{') currentDepth++;
            }
        }
        return depthInLine;
    }

    /**
     * Computes nesting area (i.e. line-wise sum of nesting levels) of the given code
     */
    public static int getNestingArea(String code) {
        int area = 0;
        for (int value : getNestingDepths(code)) area += value;
        return area;
    }

    /**
     * Computes nesting depth (i.e. line-wise maximum of nesting levels) of the given code
     */
    public static int getNestingDepth(String code) {
        int depth = 0;
        for (int value : getNestingDepths(code)) depth = Math.max(value, depth);
        return depth;
    }
}
