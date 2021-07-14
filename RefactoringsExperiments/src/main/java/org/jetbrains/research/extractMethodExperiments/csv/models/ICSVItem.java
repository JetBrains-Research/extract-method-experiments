package org.jetbrains.research.extractMethodExperiments.csv.models;

public interface ICSVItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}
