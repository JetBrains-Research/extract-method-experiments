package org.jetbrains.research.extractMethod.metrics.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FeaturesVector {
    private final List<FeatureItem> features = new ArrayList<>();
    private final int dimension;

    public FeaturesVector(int dimension) {
        this.dimension = dimension;
    }

    public void addFeature(final FeatureItem item) {
        int bestIndex = Collections.binarySearch(this.features, item, Comparator.comparing(FeatureItem::getId));
        if (bestIndex < 0) {
            bestIndex = -bestIndex - 1;
        }
        this.features.add(bestIndex, item);
    }

    public int getDimension() {
        return dimension;
    }

    public double getFeatureValue(Feature toSearch) {
        return features.get(toSearch.getId()).getValue();
    }

    public List<FeatureItem> getItems() {
        return features;
    }

    public List<Float> buildVector() {
        int itemsPtr = 0;
        List<Float> result = new ArrayList<>();
        for (int i = 0; i < dimension; ++i) {
            if (itemsPtr != features.size() && features.get(itemsPtr).getId() == i) {
                result.add((float) features.get(itemsPtr++).getValue());
            } else {
                result.add(0f);
            }
        }
        return result;
    }

    public List<Float> buildCroppedVector(List<Integer> indexList) {
        List<Float> result = new ArrayList<>();
        int itemsPtr = 0;
        for (int i : indexList) {
            if (itemsPtr != features.size() && features.get(itemsPtr).getId() == i) {
                result.add((float) features.get(itemsPtr++).getValue());
            } else {
                result.add(0f);
            }
        }
        return result;
    }
}
