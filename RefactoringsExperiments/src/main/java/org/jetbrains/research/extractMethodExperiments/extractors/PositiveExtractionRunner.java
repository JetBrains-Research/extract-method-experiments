package org.jetbrains.research.extractMethodExperiments.extractors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class PositiveExtractionRunner {
    private List<String> repos;
    private int current = 0;
    private int total = 0;
    private Logger logger;

    public PositiveExtractionRunner(List<String> repos, Logger logger) {
        this.repos = repos;
        this.logger = logger;
    }

    public void run() throws Exception {
        current = 0;
        total = Math.max(repos.size(), 1);

        for (String repo : repos) {
            String url = "https://github.com/" + repo + ".git";
            String pathToResult = repo;
            if (ExtractionConfig.noSubfolders) {
                String[] parts = repo.split(" ");
                if (parts.length >= 2) {
                    pathToResult = parts[1];
                }
            }
            String outputFileName = "results/" + pathToResult + "_results.txt";
            tryCreateFile(outputFileName);
            FileWriter fileWriter = new FileWriter(outputFileName);
            logger.log(Level.INFO, String.format("Stepped into %s repository, processed %d out of %d", repo, current, total));
            current++;
            final PrintWriter printWriter = new PrintWriter(fileWriter);
            MiningInit extractor = new MiningInit(printWriter, url, repo, logger);
            extractor.run();

        }
        //CSVBuilder.shared.finish(true);
    }

    private void tryCreateFile(String name) {
        File file = new File(name);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            logger.log(Level.ERROR, "Could not make file "+ name);
        }
    }
}
