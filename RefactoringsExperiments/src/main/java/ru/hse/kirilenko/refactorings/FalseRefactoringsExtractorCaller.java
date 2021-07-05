package ru.hse.kirilenko.refactorings;

import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.extractors.ExtractionConfig;
import ru.hse.kirilenko.refactorings.extractors.FalseRefactoringsExtractor;
import ru.hse.kirilenko.refactorings.extractors.NegExtractionRunner;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FalseRefactoringsExtractorCaller {
    public static void run(String path)  {
        try {
            SparseCSVBuilder.sharedInstance = new SparseCSVBuilder("false.csv", ExtractionConfig.nFeatures);
        } catch (Exception e){
            String errormsg = String.format("Couldn't make %s file\nExiting...", path);
            System.out.println(errormsg);
            System.exit(0);

        }
        FalseRefactoringsExtractor falseRefactoringsExtractor = new FalseRefactoringsExtractor();

        File file = new File(path);

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            //List<String> repos = new ArrayList<>();
            br.lines().forEach(s -> {
                //repos.add(s);

                String url = "https://github.com/" + s + ".git";
                try {
                    falseRefactoringsExtractor.run(s, url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });



        } catch (Exception e) {
            String errormsg = String.format("Warning, there is no such file: %s\nExiting...", path);
            System.out.println(errormsg);
            System.exit(0);
        }
    }
}