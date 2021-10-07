package org.jetbrains.research.extractMethod;

import com.intellij.openapi.application.ApplicationStarter;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethod.core.extractors.NegativeRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethod.metrics.features.Feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.research.extractMethod.BaseRunnerKt;

public class PluginRunner implements ApplicationStarter {
    private final Logger LOG = LogManager.getLogger(PluginRunner.class);
    private final int featureCount = 78;

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

        runExtractions(line);
    }

    private void runExtractions(CommandLine cmdLine) {
        StringBuilder outputDirPathBuilder = new StringBuilder();

        BaseRunner runner = new BaseRunner();
        try{
            configureOutput(outputDirPathBuilder, cmdLine);
        } catch (MissingArgumentException e) {
            LOG.error("<datasetsDirPath> is a required argument.");
            return;
        }

        Path inputDir = null;
        try {
            inputDir = Paths.get(cmdLine.getOptionValue("projectsDirPath"));
        } catch (java.nio.file.InvalidPathException e) {
            LOG.error("<projectsDirPath> has to be a valid path.");
            return;
        }

        String outputDirPath = outputDirPathBuilder.toString();

        if (cmdLine.hasOption("generatePositiveSamples")) {
            FileWriter positiveFW = null;
            try {
                positiveFW = makePositiveHeader(outputDirPath);
            } catch (Exception e) {
                LOG.error("Failed to make header for positive.csv.");
            }

            if (positiveFW != null) {
                runner.runPositives(inputDir, positiveFW);
            }
        }
        if (cmdLine.hasOption("generateNegativeSamples")) {
            FileWriter negativeFW = null;
            try {
                negativeFW = makeNegativeHeader(outputDirPath);
            } catch (IOException e) {
                LOG.error("Failed to make header for negative.csv.");
            }
            if (negativeFW != null) {
                runner.runNegatives(inputDir, negativeFW);
            }
        }
    }

    private void configureOutput(StringBuilder outputDirBuilder, CommandLine cmdLine) throws MissingArgumentException{
        if (cmdLine.hasOption("datasetsDirPath")) {
            outputDirBuilder.append(cmdLine.getOptionValue("datasetsDirPath"));
            try {
                Files.createDirectories(Paths.get(outputDirBuilder.toString()));
            } catch (IOException e) {
                LOG.error("Failed to create the output directory.");
            }
        } else {
            throw new MissingArgumentException("Missing <datasetsDirPath>.");
        }

    }

    private Options configureOptionsForCLI() {
        Options options = new Options();
        options.addOption("runner", true, "Runner name.");
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

    private FileWriter makePositiveHeader(String outputDir) throws IOException {
        FileWriter positiveFW = makeDefaultHeader(Paths.get(outputDir, "positive.csv").toString());

        positiveFW.append("\n");

        return positiveFW;
    }

    private FileWriter makeNegativeHeader(String outputDir) throws IOException {
        FileWriter negativeFW = makeDefaultHeader(Paths.get(outputDir, "negative.csv").toString());

        negativeFW.append(";Score\n");

        return negativeFW;
    }

    private FileWriter makeDefaultHeader(String filePath) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (int i = 0; i < featureCount; i++) {
            fw.append(Feature.fromId(i).getName());
            fw.append(';');
        }
        fw.append("RepositoryName");
        return fw;
    }
}
