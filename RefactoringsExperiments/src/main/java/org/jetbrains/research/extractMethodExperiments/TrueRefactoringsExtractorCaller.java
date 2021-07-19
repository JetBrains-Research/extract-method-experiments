package org.jetbrains.research.extractMethodExperiments;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.research.extractMethodExperiments.csv.SparseCSVBuilder;
import org.jetbrains.research.extractMethodExperiments.extractors.PositiveExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.utils.RepoListParser;

import java.io.IOException;

public class TrueRefactoringsExtractorCaller {
    public static void run(String path, LoggerContext context) throws IOException {
        Logger logger = context.getLogger("true-extractor");
        RepoListParser repoParser = new RepoListParser(path, context.getLogger("extract-call"));
        SparseCSVBuilder.sharedInstance = new SparseCSVBuilder("output/true.csv", 117);
        PositiveExtractionRunner runner = new PositiveExtractionRunner(repoParser.getRepositories(), logger);
        try {
            runner.run();
        } catch (Exception e){
            logger.log(Level.ERROR, e.getMessage());
        }
    }
}
