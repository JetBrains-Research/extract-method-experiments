package org.jetbrains.research.extractMethod.metrics.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeatureItem;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.jetbrains.research.extractMethod.metrics.location.LocationItem;
import org.jetbrains.research.extractMethod.metrics.location.LocationVector;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class DatasetRecord {
    private final JSONObject jsonRecord = new JSONObject();

    public DatasetRecord(FeaturesVector featuresVector, LocationVector locationVector,
                         double score, String rawCode) {
        for (FeatureItem item : featuresVector.getItems()) {
            jsonRecord.put(item.getName(), item.getValue());
        }

        for (LocationItem item : locationVector.getItems()) {
            jsonRecord.put(item.getName(), item.getValue());
        }

        jsonRecord.put("Score", score);
        jsonRecord.put("Code", rawCode);
    }

    public void writeRecord(FileWriter fw) throws IOException {
        fw.write(jsonRecord + ",\n");
    }
}
