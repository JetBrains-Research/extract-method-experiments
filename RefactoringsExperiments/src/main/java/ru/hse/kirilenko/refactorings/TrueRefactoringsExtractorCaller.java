package ru.hse.kirilenko.refactorings;

import ru.hse.kirilenko.refactorings.extractors.ExtractionConfig;
import ru.hse.kirilenko.refactorings.extractors.PosExtractionRunner;
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrueRefactoringsExtractorCaller {
    public static void run(String path) throws Exception {
        File file = new File(path);
        List<String> repos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(repos::add);
        } catch (Exception e) {
            String errormsg = String.format("Warning, there is no such file: %s\nExiting...", path);
            System.out.println(errormsg);
            System.exit(0);
        }
        SparseCSVBuilder.sharedInstance = new SparseCSVBuilder("true.csv", ExtractionConfig.nFeatures);
        PosExtractionRunner runner = new PosExtractionRunner(repos);
        runner.run();
    }
}
