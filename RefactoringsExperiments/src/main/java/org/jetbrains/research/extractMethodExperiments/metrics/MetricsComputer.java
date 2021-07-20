package org.jetbrains.research.extractMethodExperiments.metrics;

import org.jetbrains.research.extractMethodExperiments.haas.Candidate;

import java.util.HashMap;
import java.util.Map;

public class MetricsComputer {
    public static Map<Integer, Double> computeMetrics(Candidate candidate){
        Map<Integer, Double> metricMap = new HashMap<>();

        computeKeywordMetrics(candidate, metricMap);
        return metricMap;
    }

    public static void computeKeywordMetrics(Candidate candidate, Map<Integer, Double> metricMap){

    }
}
