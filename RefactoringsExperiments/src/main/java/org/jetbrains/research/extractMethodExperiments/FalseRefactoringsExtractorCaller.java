package org.jetbrains.research.extractMethodExperiments;

import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.extractors.FalseRefactoringsExtractor;
import org.jetbrains.research.extractMethodExperiments.utils.RepoListParser;
import org.jetbrains.research.extractMethodExperiments.extractors.ExtractionConfig;

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

    public static void run(String path) throws IOException {
        String outFilePath = "false.csv";

        FileWriter fw = new FileWriter(outFilePath);
        makeFileHeader(fw);

        FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor(fw);
        RepoListParser repoParser = new RepoListParser(path);
        List<String> repositories = repoParser.getRepositories();
        for (int i = 0; i < repositories.size(); i++) {
            String repoName = repositories.get(i);
            System.out.printf("%d/%d, at %s", i + 1, repositories.size(), repoName);
            String url = "https://github.com/" + repoName + ".git";
            try {
                falseRefactoringsExtractor.run(repoName, url);
            } catch (Exception e) {
                e.printStackTrace(); //make logger call
            }
        }
    }

}