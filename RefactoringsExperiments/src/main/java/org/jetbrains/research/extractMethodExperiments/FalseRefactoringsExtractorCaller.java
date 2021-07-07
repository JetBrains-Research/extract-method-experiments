package org.jetbrains.research.extractMethodExperiments;

import org.jetbrains.research.extractMethodExperiments.csv.models.Feature;
import org.jetbrains.research.extractMethodExperiments.extractors.FalseRefactoringsExtractor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FalseRefactoringsExtractorCaller {
    static void makeFileHeader(FileWriter fw) throws IOException {
        for (int i = 0; i < 117; i++)
            fw.write(Feature.fromId(i).getName() + ';');
        fw.write("score;");
        fw.write("label\n");
    }

    public static void run(String path) throws IOException {
        String outFilePath = "false.csv";

        FileWriter fw = new FileWriter(outFilePath);
        makeFileHeader(fw);

        FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor(fw);

        File file = new File(path);
        List<String> repos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(repos::add);
        } catch (Exception e) {
            System.out.printf("Warning, there is no such file: %s\nExiting...\n", path);
            System.exit(-1);
        }
        for (int i = 0; i < repos.size(); i++) {
            String repoName = repos.get(i);
            System.out.printf("%d/%d, at %s", i + 1, repos.size(), repoName);
            String url = "https://github.com/" + repoName + ".git";
            try {
                falseRefactoringsExtractor.run(repoName, url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}