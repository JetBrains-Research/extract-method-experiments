package org.jetbrains.research.extractMethodExperiments.features;

public interface IFeatureItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}