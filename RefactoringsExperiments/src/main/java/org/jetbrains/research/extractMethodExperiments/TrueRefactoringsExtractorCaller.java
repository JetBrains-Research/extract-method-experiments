package org.jetbrains.research.extractMethodExperiments;

import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.extractors.ExtractionConfig;
import org.jetbrains.research.extractMethodExperiments.extractors.PositiveExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.utils.RepoListParser;

public class TrueRefactoringsExtractorCaller {
    public static void run(String path, LoggerContext context) throws Exception {
        RepoListParser repoParser = new RepoListParser(path, context.getLogger("extract-call"));
        SparseCSVBuilder.sharedInstance = new SparseCSVBuilder("output/true.csv", ExtractionConfig.nFeatures);
        PositiveExtractionRunner runner = new PositiveExtractionRunner(repoParser.getRepositories(), context.getLogger("true-extractor"));
        runner.run();
    }
}