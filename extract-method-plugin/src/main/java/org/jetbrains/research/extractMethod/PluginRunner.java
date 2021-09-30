package org.jetbrains.research.extractMethod;

import com.intellij.openapi.application.ApplicationStarter;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethod.core.extractors.NegativeRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethod.core.extractors.PositiveRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethod.metrics.features.Feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
            LOG.error("[RefactoringJudge]: Failed to parse command-line arguments.");
        }
        if (line == null) return;

        runExtractions(line);
    }

    private void runExtractions(CommandLine cmdLine) {
        List<String> projectPaths = new ArrayList<>();
        StringBuilder outputDirPathBuilder = new StringBuilder();

        configureIO(projectPaths, outputDirPathBuilder, cmdLine);

        String outputDirPath = outputDirPathBuilder.toString();

        if (cmdLine.hasOption("generatePositiveSamples")) {
            FileWriter positiveFW = null;
            try {
                positiveFW = makePositiveHeader(outputDirPath);
            } catch (Exception e) {
                LOG.error("[RefactoringJudge]: Failed to make header for positive.csv.");
            }

            if (positiveFW != null) {
                PositiveRefactoringsExtractionRunner positiveRefactoringsExtractionRunner = new PositiveRefactoringsExtractionRunner(projectPaths, positiveFW);
                positiveRefactoringsExtractionRunner.run();
            }
        }
        if (cmdLine.hasOption("generateNegativeSamples")) {
            FileWriter negativeFW = null;
            try {
                negativeFW = makeNegativeHeader(outputDirPath);
            } catch (IOException e) {
                LOG.error("[RefactoringJudge]: Failed to make header for negative.csv.");
            }
            if (negativeFW != null) {
                NegativeRefactoringsExtractionRunner negativeRefactoringsExtractionRunner = new NegativeRefactoringsExtractionRunner(projectPaths, negativeFW);
                negativeRefactoringsExtractionRunner.run();
            }
        }

        if (cmdLine.hasOption("generateNegativeSamples")) {
            FileWriter negativeFW = null;
            try {
                negativeFW = makeNegativeHeader(outputDirPath);
            } catch (IOException e) {
                LOG.error("[RefactoringJudge]: Failed to make header for negative.csv.");
            }
            if (negativeFW != null) {
                NegativeRefactoringsExtractionRunner negativeRefactoringsExtractionRunner = new NegativeRefactoringsExtractionRunner(projectPaths, negativeFW);
                negativeRefactoringsExtractionRunner.run();
            }
        }
    }

    private void configureIO(List<String> inRepoPaths, StringBuilder outputDirBuilder, CommandLine cmdLine) {
        if (cmdLine.hasOption("projectsDirPath")) {
            String projectsFilePath = cmdLine.getOptionValue("projectsDirPath");
            inRepoPaths.addAll(extractProjectsPaths(projectsFilePath));
        } else {
            LOG.error("[RefactoringJudge]: Projects directory is mandatory.");
            return;
        }

        if (cmdLine.hasOption("datasetsDirPath")) {
            outputDirBuilder.append(cmdLine.getOptionValue("datasetsDirPath"));
            try {
                Files.createDirectories(Paths.get(outputDirBuilder.toString()));
            } catch (IOException e) {
                LOG.error("[RefactoringJudge]: Failed to create output directory.");
            }
        } else {
            LOG.error("[RefactoringJudge]: Output directory is mandatory.");
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
