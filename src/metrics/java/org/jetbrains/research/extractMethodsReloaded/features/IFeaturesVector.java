package org.jetbrains.research.extractMethodsReloaded.features;

import java.util.List;

public interface IFeaturesVector {
    void addFeature(final IFeatureItem item);

    double getFeature(final Feature item);

    List<Float> buildVector();
}
