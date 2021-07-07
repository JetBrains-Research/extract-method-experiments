package org.jetbrains.research.extractMethodExperiments.extractors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PosExtractionRunner {
    private List<String> repos = new ArrayList<>();
    private int current = 0;
    private int total = 0;

    public PosExtractionRunner(List<String> repos) {
        this.repos = repos;
    }

    public void run() throws Exception {
        current = 0;
        total = Math.max(repos.size(), 1);

        for (String repo : repos) {
            current++;
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
            System.out.printf("%d out of %d, running repo with URL:%s\n", current, total, url);
            final PrintWriter printWriter = new PrintWriter(fileWriter);
            MiningInit extractor = new MiningInit(printWriter, url, repo);
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
            e.printStackTrace();
        }
    }
}
