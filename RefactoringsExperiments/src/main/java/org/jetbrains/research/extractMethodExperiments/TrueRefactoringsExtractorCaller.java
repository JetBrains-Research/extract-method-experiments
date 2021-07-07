package org.jetbrains.research.extractMethodExperiments;

import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.extractors.ExtractionConfig;
import org.jetbrains.research.extractMethodExperiments.extractors.PositiveExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.utils.RepoListParser;

public class TrueRefactoringsExtractorCaller {
    public static void run(String path) throws Exception {
        RepoListParser repoParser = new RepoListParser(path);
        SparseCSVBuilder.sharedInstance = new SparseCSVBuilder("true.csv", ExtractionConfig.nFeatures);
        PositiveExtractionRunner runner = new PositiveExtractionRunner(repoParser.getRepositories());
        runner.run();
    }
}