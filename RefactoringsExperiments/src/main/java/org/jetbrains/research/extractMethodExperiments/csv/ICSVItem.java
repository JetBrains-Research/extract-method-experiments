package org.jetbrains.research.extractMethodExperiments.csv;

public interface ICSVItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}
