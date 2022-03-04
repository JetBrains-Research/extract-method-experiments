package org.jetbrains.research.extractMethod.metrics.location;

import org.jetbrains.research.extractMethod.metrics.features.Feature;

public class LocationItem {
    private final LocationBasis type;
    private String value;

    public LocationItem(LocationBasis type, String value) {
        this.type = type;
        this.value = value;
    }

    public LocationItem(LocationBasis type, int value) {
        this.type = type;
        this.value = String.valueOf(value);
    }

    public int getId() {
        return type.getId();
    }

    public String getName() {
        return type.getName();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String newValue) {
        this.value = newValue;
    }
}
