package org.jetbrains.research.extractMethodExperiments.csv;

import org.jetbrains.research.extractMethodExperiments.features.Feature;

public class CSVItem implements ICSVItem {
    private final Feature type;
    private double value;

    public CSVItem(Feature type, double value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public int getId() {
        return type.getId();
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void setValue(double newValue) {
        this.value = newValue;
    }
}
