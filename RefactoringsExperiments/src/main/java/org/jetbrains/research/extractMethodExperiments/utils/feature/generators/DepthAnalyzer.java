package org.jetbrains.research.extractMethodExperiments.utils.feature.generators;

public class DepthAnalyzer {
    public static int[] getNestingDepths(String code) {
        String[] lines = code.split("\n");
        int depthInLine[] = new int[lines.length];
        int currentDepth = 0; // current count

        // Traverse the input strings
        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lines[i].length(); j++) {
                if (lines[i].charAt(j) == '{') break;

                if (lines[i].charAt(j) == '}') {
                    currentDepth--;
                }
            }
            depthInLine[i] = currentDepth;
            for (int j = 0; j < lines[i].length(); j++) {
                if (lines[i].charAt(j) == '{') currentDepth++;
            }
        }
        return depthInLine;
    }

    public static int getNestingArea(String code) {
        int area = 0;
        for (int value : getNestingDepths(code)) area += value;
        return area;
    }

    public static int getNestingDepth(String code) {
        int depth = 0;
        for (int value : getNestingDepths(code)) depth = Math.max(value, depth);
        return depth;
    }
}
