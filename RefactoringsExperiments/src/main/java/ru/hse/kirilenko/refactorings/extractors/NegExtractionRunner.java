package ru.hse.kirilenko.refactorings.extractors;

import ru.hse.kirilenko.refactorings.MiningCaller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NegExtractionRunner {
    private List<String> repos = new ArrayList<>();
    private int current = 0;
    private int total = 0;
    public NegExtractionRunner(List<String> repos) {
        this.repos = repos;
    }

    public void run() {
        total = 0;
        current = 0;
        total = Math.max(repos.size(), 1);

        for (String repo: repos) {

        }
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
