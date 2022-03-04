package org.jetbrains.research.extractMethod.core.utils;

import org.jetbrains.research.extractMethod.metrics.location.LocationBasis;
import org.jetbrains.research.extractMethod.metrics.location.LocationItem;
import org.jetbrains.research.extractMethod.metrics.location.LocationVector;

public class LocationUtil {
    public static LocationVector buildLocationVector(String repositoryName, String commit, String filePath, int beginLine, int endLine) {
        LocationVector locationVector = new LocationVector();

        locationVector.addFeature(new LocationItem(LocationBasis.Repository, repositoryName));
        locationVector.addFeature(new LocationItem(LocationBasis.Commit, commit));
        locationVector.addFeature(new LocationItem(LocationBasis.FilePath, filePath));
        locationVector.addFeature(new LocationItem(LocationBasis.BeginLine, beginLine));
        locationVector.addFeature(new LocationItem(LocationBasis.EndLine, endLine));

        return locationVector;
    }
}
