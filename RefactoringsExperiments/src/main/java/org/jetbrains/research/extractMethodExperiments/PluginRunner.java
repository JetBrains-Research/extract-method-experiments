package org.jetbrains.research.extractMethodExperiments;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.extractors.NegativeRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.extractors.PositiveRefactoringsExtractionRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PluginRunner implements ApplicationStarter {
    private final Logger LOG = Logger.getInstance(PluginRunner.class);

    @Override
    public @NonNls
    String getCommandName() {
        return "RefactoringsExperiments";
    }

    @Override
    public void main(@NotNull List<String> args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(configureOptionsForCLI(), args.toArray(new String[0]));
        } catch (ParseException e) {
            LOG.error("Failed to parse command-line arguments.");
        }
        if (line == null) return;

        List<String> projectPaths = new ArrayList<>();
        if (line.hasOption("projectsDirPath")) {
            String projectsFilePath = line.getOptionValue("projectsDirPath");
            projectPaths = extractProjectsPaths(projectsFilePath);
        } else {
            LOG.error("Projects directory is mandatory.");
            return;
        }

        String outputDir = null;

        if (line.hasOption("datasetsDirPath")) {
            outputDir = line.getOptionValue("datasetsDirPath");
            try {
                Files.createDirectories(Paths.get(outputDir));
            } catch (IOException e) {
                LOG.error("Failed to create output dir");
            }
        } else {
            LOG.error("Output directory is mandatory.");
        }

        if (line.hasOption("generatePositiveSamples")) {
            try {
                FileWriter positiveFW = new FileWriter(Paths.get(outputDir, "positive.csv").toString());
            } catch (IOException e) {
                LOG.error("Failed to create file-writer for positive samples");
            }

            PositiveRefactoringsExtractionRunner positiveRefactoringsExtractionRunner = new PositiveRefactoringsExtractionRunner(projectPaths);
            positiveRefactoringsExtractionRunner.run();
        }
        if (line.hasOption("generateNegativeSamples")) {

            try {
                FileWriter negativeFW = new FileWriter(Paths.get(outputDir, "negative.csv").toString());
            } catch (IOException e) {
                LOG.error("Failed to create file-writer for negative samples");
            }

            NegativeRefactoringsExtractionRunner negativeRefactoringsExtractionRunner = new NegativeRefactoringsExtractionRunner(projectPaths);
            negativeRefactoringsExtractionRunner.run();
        }
    }


    private Options configureOptionsForCLI() {
        Options options = new Options();
        options.addOption("runner", false, "Runner name.");
        options.addRequiredOption("paths", "projectsDirPath", true, "Path to the file containing paths to the projects for dataset.");
        options.addRequiredOption("out", "datasetsDirPath", true, "Desired path to the output directory.");
        options.addOption("p", "generatePositiveSamples", false, "Runs generation of positive samples for dataset.");
        options.addOption("n", "generateNegativeSamples", false, "Runs generation of negative samples for dataset.");
        return options;
    }

    private List<String> extractProjectsPaths(String path) {
        ArrayList<String> paths = new ArrayList<>();
        File reposDir = new File(path);

        for (File repoFile : reposDir.listFiles()) {
            paths.add(repoFile.getAbsolutePath());
        }

        return paths;
    }
}
