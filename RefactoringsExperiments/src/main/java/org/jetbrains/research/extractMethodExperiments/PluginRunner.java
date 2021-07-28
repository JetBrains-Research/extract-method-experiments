package org.jetbrains.research.extractMethodExperiments;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.extractors.NegativeRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.extractors.PositiveRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.features.Feature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PluginRunner implements ApplicationStarter {
    private final Logger LOG = Logger.getInstance(PluginRunner.class);
    private final int featureCount = 82;

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

        List<String> projectPaths;
        if (line.hasOption("projectsDirPath")) {
            String projectsFilePath = line.getOptionValue("projectsDirPath");
            projectPaths = extractProjectsPaths(projectsFilePath);
        } else {
            LOG.error("[RefactoringJudge]: Projects directory is mandatory.");
            return;
        }

        String outputDir = null;

        if (line.hasOption("datasetsDirPath")) {
            outputDir = line.getOptionValue("datasetsDirPath");
            try {
                Files.createDirectories(Paths.get(outputDir));
            } catch (IOException e) {
                LOG.error("[RefactoringJudge]: Failed to create output directory.");
            }
        } else {
            LOG.error("[RefactoringJudge]: Output directory is mandatory.");
        }

        if (line.hasOption("generatePositiveSamples")) {
            FileWriter positiveFW = null;
            try {
                positiveFW = makePositiveHeader(outputDir);
            } catch (Exception e) {
                LOG.error("[RefactoringJudge]: Failed to make header for positive.csv.");
            }

            if (positiveFW != null) {
                PositiveRefactoringsExtractionRunner positiveRefactoringsExtractionRunner = new PositiveRefactoringsExtractionRunner(projectPaths, positiveFW);
                positiveRefactoringsExtractionRunner.run();
            }
        }

        if (line.hasOption("generateNegativeSamples")) {
            FileWriter negativeFW = null;
            try {
                negativeFW = makeNegativeHeader(outputDir);
            } catch (IOException e) {
                LOG.error("[RefactoringJudge]: Failed to make header for negative.csv.");
            }
            if (negativeFW != null) {
                NegativeRefactoringsExtractionRunner negativeRefactoringsExtractionRunner = new NegativeRefactoringsExtractionRunner(projectPaths, negativeFW);
                negativeRefactoringsExtractionRunner.run();
            }
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

    private FileWriter makePositiveHeader(String outputDir) throws IOException {
        FileWriter positiveFW = new FileWriter(Paths.get(outputDir, "positive.csv").toString());
        for (int i = 0; i < featureCount; i++) {
            positiveFW.append(Feature.fromId(i).getName());
            positiveFW.append(';');
        }
        positiveFW.append('\n');
        return positiveFW;
    }

    private FileWriter makeNegativeHeader(String outputDir) throws IOException {
        FileWriter negativeFW = new FileWriter(Paths.get(outputDir, "negative.csv").toString());
        for (int i = 0; i < featureCount; i++) {
            negativeFW.append(Feature.fromId(i).getName());
            negativeFW.append(';');
        }
        negativeFW.append("Score;\n");

        return negativeFW;
    }
}
