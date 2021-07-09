package org.jetbrains.research.extractMethodExperiments.utils.feature.generators;

import java.util.Set;

public class CouplingCalculator {
    public static int calcConnectivity(String code, Set<String> members) {
        int res = 0;
        for (String member : members) {
            if (code.contains(member)) {
                res += 1;
            }
        }

        return res;
    }
}
