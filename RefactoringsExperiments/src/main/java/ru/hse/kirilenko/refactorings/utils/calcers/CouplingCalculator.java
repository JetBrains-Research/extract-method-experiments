package ru.hse.kirilenko.refactorings.utils.calcers;

import java.util.Set;

public class CouplingCalculator {
    public static int calcConnectivity(String code, Set<String> members) {
        int res = 0;
        for (String member: members) {
            if (code.contains(member)) {
                res += 1;
            }
        }

        return res;
    }
}
