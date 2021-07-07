package org.jetbrains.research.extractMethodExperiments.utils.calcers;

import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.csv.models.CSVItem;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.csv.models.ICSVItem;
import org.jetbrains.research.extractMethodExperiments.utils.trie.Trie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeywordsCalculator {
    public static List<String> allKeywords = Arrays.asList(
            "abstract", "continue", "for", "new", "switch", "assert",
            "default", "package", "synchronized", "boolean", "do", "if",
            "private", "this", "break", "double", "implements", "protected",
            "throw", "byte", "else", "import", "public", "throws", "case",
            "enum", "instanceof", "return", "transient", "catch", "extends",
            "int", "short", "try", "char", "final", "interface", "static",
            "void", "class", "finally", "long", "strictfp", "volatile",
            "const", "float", "native", "super", "while");

    public static Trie keywordsTrie = new Trie(allKeywords);

    public static void calculateCSV(String codeFragmentString, int fragLinesCount) {
        HashMap<String, Integer> counts = KeywordsCalculator.keywordsTrie.calculate(codeFragmentString);

        int id = 6; // initialized with 6 to account for shift in KeywordFeatures begin id.
        for (String keyword: KeywordsCalculator.allKeywords) {
            Integer count = counts.get(keyword);
            if (count == null) {
                count = 0;
            }

            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.fromId(id++), count));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.fromId(id++), (double)count / fragLinesCount));
        }
    }

    public static void extractToList(String codeFragmentString, List<ICSVItem> features, int fragLinesCount) {
        HashMap<String, Integer> counts = KeywordsCalculator.keywordsTrie.calculate(codeFragmentString);
        int id = 6; // initialized with 6 to account for shift in KeywordFeatures begin id.
        for (String keyword: KeywordsCalculator.allKeywords) {
            Integer count = counts.get(keyword);
            if (count == null) {
                count = 0;
            }

            features.add(new CSVItem(Feature.fromId(id++), (double) count));
            features.add(new CSVItem(Feature.fromId(id++), (double)count / fragLinesCount));
        }
    }

}
