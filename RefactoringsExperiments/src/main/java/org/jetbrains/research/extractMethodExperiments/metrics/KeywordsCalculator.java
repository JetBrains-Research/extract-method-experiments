package org.jetbrains.research.extractMethodExperiments.metrics;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.research.extractMethodExperiments.csv.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.Feature;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeywordsCalculator {
    public static List<String> allKeywords = Arrays.asList(
            "continue", "for", "new", "switch",
            "assert", "synchronized", "boolean", "do",
            "if","this", "break", "double",
            "throw", "byte", "else", "throws", "case",
            "enum", "instanceof", "return", "transient",
            "catch", "int", "short", "try",
            "char", "final", "finally", "long",
            "strictfp", "float", "super", "while"); // 33 keywords, id from 6 to

    public static void calculateCSV(String codeFragmentString, int fragmentLinesCount) {
        HashMap<String, Integer> counts = new HashMap<>();
        for (String key : allKeywords) {
            counts.put(key, StringUtils.countMatches(codeFragmentString, key));
        }

        int id = 6; // initialized with 6 to account for shift in KeywordFeatures begin id.
        for (String keyword : KeywordsCalculator.allKeywords) {
            Integer count = counts.get(keyword);
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.fromId(id++), count));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.fromId(id++), (double) count / fragmentLinesCount));
        }
    }

}
