package org.jetbrains.research.extractMethodExperiments.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/** Utility class for tidy parsing of repository lists in the provided file */
public class RepoListParser {

    private String path;
    public RepoListParser(String path){
        this.path = path;
    }

    public List<String> getRepositories(){
        File file = new File(path);
        List<String> repos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(repos::add);
        } catch (Exception e) {
            System.out.printf("There is no such file: %s\n", path);
        }
        return repos;
    }
}
