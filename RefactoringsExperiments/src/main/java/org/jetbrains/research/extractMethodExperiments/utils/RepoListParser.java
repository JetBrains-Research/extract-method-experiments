package org.jetbrains.research.extractMethodExperiments.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for tidy parsing of repository lists in the provided file
 */
public class RepoListParser {

    private String path;
    private Logger logger;

    public RepoListParser(String path, Logger logger) {
        this.path = path; this.logger = logger;
    }

    public List<String> getRepositories() {
        File file = new File(path);
        List<String> repos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.lines().forEach(repos::add);
        } catch (Exception e) {
            logger.log(Level.ERROR, "There is no such file: " + path);
        }
        return repos;
    }
}
