package org.jetbrains.research.extractMethodExperiments;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.extractMethodExperiments.extractors.NegativeRefactoringsExtractionRunner;
import org.jetbrains.research.extractMethodExperiments.extractors.PositiveRefactoringsExtractionRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
        System.out.println(args);
        if (line.hasOption("projectsFilePath")) {
            String projectsFilePath = line.getOptionValue("projectsFilePath");
            projectPaths = extractProjectsPaths(projectsFilePath);
        }
        if (line.hasOption("generatePositiveSamples")) {
            PositiveRefactoringsExtractionRunner positiveRefactoringsExtractionRunner = new PositiveRefactoringsExtractionRunner(projectPaths);
            positiveRefactoringsExtractionRunner.run();
        }
        if (line.hasOption("generateNegativeSamples")) {
            NegativeRefactoringsExtractionRunner negativeRefactoringsExtractionRunner = new NegativeRefactoringsExtractionRunner(projectPaths);
            negativeRefactoringsExtractionRunner.run();
        }
    }

    private Options configureOptionsForCLI() {
        Options options = new Options();
        options.addOption("runner", false, "Runner name.");
        options.addRequiredOption("paths", "projectsFilePath", true, "Path to the file containing paths to the projects for dataset.");
        options.addOption("p", "generatePositiveSamples", false, "Runs generation of positive samples for dataset.");
        options.addOption("n", "generateNegativeSamples", false, "Runs generation of negative samples for dataset.");
        return options;
    }

    private List<String> extractProjectsPaths(String path) {
        ArrayList<String> paths = new ArrayList<>();
        File directory = new File("C:\\Users\\vultu\\CodingStuff\\jbr\\extract-method-experiments\\RefactoringsExperiments\\data");
        System.out.println(directory.getAbsolutePath());

        File reposDir = new File(path);
        System.out.println(path);

        System.out.println(Arrays.toString(reposDir.list()));
        //TODO: parse the file and extract paths to the projects
        return paths;
    }
}
