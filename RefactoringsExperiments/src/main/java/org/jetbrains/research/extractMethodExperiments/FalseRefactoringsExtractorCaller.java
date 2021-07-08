package org.jetbrains.research.extractMethodExperiments;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.extractors.ExtractionConfig;
import org.jetbrains.research.extractMethodExperiments.extractors.FalseRefactoringsExtractor;
import org.jetbrains.research.extractMethodExperiments.utils.RepoListParser;

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
        String outFilePath = "false.csv";

        FileWriter fw = new FileWriter(outFilePath);
        makeFileHeader(fw);

        Logger logger = context.getLogger("extract-call");
        logger.log(Level.INFO, "Made header for file "+outFilePath);

        FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor(fw, context);
        RepoListParser repoParser = new RepoListParser(path);
        List<String> repositories = repoParser.getRepositories();
        for (int i = 0; i < repositories.size(); i++) {
            String repoName = repositories.get(i);
            logger.log(Level.INFO, String.format("Stepped into %s repository, processed %d out of %d", repoName, i, repositories.size()));
            String url = "https://github.com/" + repoName + ".git";

            falseRefactoringsExtractor.run(repoName, url);

        }
    }

}