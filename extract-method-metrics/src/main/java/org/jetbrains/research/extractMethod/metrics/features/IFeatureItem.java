package org.jetbrains.research.extractMethod.metrics.features;

public interface IFeatureItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}