package org.jetbrains.research.extractMethods.features;

public interface IFeatureItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}