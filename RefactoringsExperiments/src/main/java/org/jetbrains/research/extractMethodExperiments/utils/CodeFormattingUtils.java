package org.jetbrains.research.extractMethodExperiments.utils;

import org.apache.commons.lang3.StringUtils;

public class CodeFormattingUtils {

    public static boolean isUselessChar(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
    }

    /**
     * Clears the passed code from redundant curly braces and trims end-lines, tabs and whitespaces.
     * Also removes all types of comments
     */
    public static String clearCode(String code) {
        int trailingBracesToRemove = 0;
        char[] charArray = code.toCharArray();
        int index = 0;
        while (isUselessChar(charArray[index]) || charArray[index] == '{') {
            if (charArray[index] == '{') trailingBracesToRemove++;
            charArray[index] = ' ';
            index++;
        }
        index = charArray.length - 1;
        while (trailingBracesToRemove > 0) {
            if (charArray[index] == '}') {
                charArray[index] = ' ';
                trailingBracesToRemove--;
            }
            index--;
        }
        return String.valueOf(charArray).
                replaceAll("((/\\*)[^/]+(\\*/))|(//.*)", ""). //Comments
                replaceAll("(?m)^[ \t]*\r?\n", ""). //End-of-lines, whitespace, carriage returns
                replaceAll("^[\t\n\r}]", " ").strip(); //Closing braces (tmp)
    }

    public static int countLines(String code) {
        return StringUtils.countMatches(code, "\n") + 1;
    }
}
