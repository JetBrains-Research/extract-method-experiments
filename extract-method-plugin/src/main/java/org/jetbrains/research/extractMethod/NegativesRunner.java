package org.jetbrains.research.extractMethod;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.research.extractMethod.core.extractors.NegativesExtractor;
import org.jetbrains.research.extractMethod.core.extractors.RefactoringsExtractor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NegativesRunner extends BaseRunner {
    @Override
    public @NonNls
    String getCommandName() {
        return "NegativeRefactorings";
    }

    @Override
    void extractRefactorings(CommandLine cmdLine) {
        ExtractionRunner runner = new ExtractionRunner();
        try {
            RunnerUtils.configureOutput(cmdLine);
        } catch (MissingArgumentException e) {
            LOG.error("<outputFilePath> is a required argument.");
            return;
        } catch (IOException e) {
            LOG.error("Failed to create output directory.");
        }

        FileWriter negativeFW = null;
        try {
            negativeFW = RunnerUtils.makeNegativeHeader(cmdLine.getOptionValue("outputFilePath"), featureCount);
        } catch (IOException e) {
            LOG.error("Failed to make header for output file");
            return;
        }

        RefactoringsExtractor extractor = new NegativesExtractor(negativeFW);
        Path projectPath;
        try {
            projectPath = Paths.get(cmdLine.getOptionValue("inputProjectPath"));
        } catch (InvalidPathException e) {
            LOG.error("<inputProjectPath> has to be a valid path");
            return;
        }

        try {
            runner.runSingleExtraction(projectPath, extractor);
        } catch (Exception e) {
            LOG.error("Unexpected error in negative" +
                    " samples' procedure. \n" + e.getMessage());
        }
    }


    @Override
    Options configureOptionsForCLI() {
        Options options = new Options();
        options.addRequiredOption("input", "inputProjectPath", true, "Path to the input project.");
        options.addRequiredOption("output", "outputFilePath", true, "Path to the desired output destination.");
        return options;
    }
}
