package org.jetbrains.research.extractMethodExperiments;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.extractors.ExtractionConfig;
import org.jetbrains.research.extractMethodExperiments.extractors.FalseRefactoringsExtractor;
import org.jetbrains.research.extractMethodExperiments.utils.RepoListParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FalseRefactoringsExtractorCaller {
    static void makeFileHeader(FileWriter fw) throws IOException {
        for (int i = 0; i < ExtractionConfig.nFeatures; i++)
            fw.write(Feature.fromId(i).getName() + ';');
        fw.write("score;");
        fw.write("label\n");
    }

    public static void run(String path, LoggerContext context) throws Exception {
        Logger logger = context.getLogger("extract-call");
        try {
            new File("output/").mkdir();
        } catch (Exception e){
            logger.log(Level.ERROR, "Failed to make output directory");
        }
        String outFilePath = "output/false.csv";
        FileWriter fw = new FileWriter(outFilePath);
        makeFileHeader(fw);

        logger.log(Level.INFO, "Made header for file " + outFilePath);

        FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor(fw, context.getLogger("false-extractor"));
        RepoListParser repoParser = new RepoListParser(path, logger);
        List<String> repositories = repoParser.getRepositories();
        for (int i = 0; i < repositories.size(); i++) {
            String repoName = repositories.get(i);
            logger.log(Level.INFO, String.format("Stepped into %s repository, processed %d out of %d", repoName, i, repositories.size()));
            String url = "https://github.com/" + repoName + ".git";

            falseRefactoringsExtractor.run(repoName, url);

        }
    }

}