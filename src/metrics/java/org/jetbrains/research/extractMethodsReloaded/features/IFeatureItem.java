package org.jetbrains.research.extractMethodsReloaded.features;

public interface IFeatureItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}