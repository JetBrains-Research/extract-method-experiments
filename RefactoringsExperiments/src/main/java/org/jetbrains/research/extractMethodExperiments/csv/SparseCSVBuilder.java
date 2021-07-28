package org.jetbrains.research.extractMethodExperiments.csv;

import org.jetbrains.research.extractMethodExperiments.features.Feature;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SparseCSVBuilder {
    public static SparseCSVBuilder sharedInstance;
    private final PrintWriter pw;
    private final List<ICSVItem> items = new ArrayList<>();
    private final int nFeatures;

    public SparseCSVBuilder(final String fileName, int nFeatures) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        final PrintWriter printWriter = new PrintWriter(fileWriter);
        this.pw = printWriter;
        this.nFeatures = nFeatures;
    }

    public void addFeature(final ICSVItem item) {
        this.items.add(item);
    }

    public void writeVector(boolean label) {
        if (items.isEmpty()) {
            for (Feature feature : Feature.values()) {
                pw.print(feature.getName() + ";");
            }

            pw.print("label\n");
            return;
        }

        items.sort(Comparator.comparingInt(ICSVItem::getId));
        int itemsPtr = 0;
        for (int i = 0; i < nFeatures; ++i) {
            if (itemsPtr == items.size()) {
                pw.print("0;");
            } else {
                if (items.get(itemsPtr).getId() == i) {
                    pw.print(items.get(itemsPtr).getValue() + ";");
                    itemsPtr++;
                } else {
                    pw.print("0;");
                }
            }
        }

        pw.print(label ? "1\n" : "0\n");

        items.clear();
    }
}
