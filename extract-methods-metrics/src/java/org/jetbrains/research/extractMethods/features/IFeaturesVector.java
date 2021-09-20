package org.jetbrains.research.extractMethods.features;

import java.util.List;

public interface IFeaturesVector {
    void addFeature(final IFeatureItem item);

    double getFeature(final Feature item);

    List<Float> buildVector();
}
