package org.jetbrains.research.extractMethod.metrics.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationVector {
    private final List<LocationItem> location = new ArrayList<>();

    public LocationVector() {
    }

    public void addFeature(final LocationItem item) {
        int bestIndex = Collections.binarySearch(this.location, item, Comparator.comparing(LocationItem::getId));
        if (bestIndex < 0) {
            bestIndex = -bestIndex - 1;
        }
        this.location.add(bestIndex, item);
    }

    public LocationItem getLocationItem(LocationBasis toSearch) {
        return location.get(toSearch.getId());
    }

    public List<LocationItem> getItems() {
        return location;
    }
}
