package org.jetbrains.research.extractMethod.metrics.location;

public enum LocationBasis {
    Repository("Repository", 0),
    Commit("Commit", 1),
    FilePath("FilePath", 2),
    BeginLine("BeginLine", 3),
    EndLine("EndLine", 4);

    private final String name;
    private final int id;

    LocationBasis(String name, int id) {
        this.name = name;
        this.id = id;
    }
    public static LocationBasis fromId(int id) {
        return LocationBasis.values()[id];
    }
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
}
