package ru.hse.kirilenko.refactorings.collectors;

import java.util.HashMap;

public class LocCollector {
    public static HashMap<Integer, Integer> counts;

    public static void accept(int value) {
        //counts.put(value, counts.getOrDefault(value, 0) + 1);
    }
}
