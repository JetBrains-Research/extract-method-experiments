package org.jetbrains.research.extractMethod;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.research.extractMethod.core.extractors.PositivesExtractor;
import org.jetbrains.research.extractMethod.core.extractors.RefactoringsExtractor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Plugin starter for extraction of negative refactorings
 */
public class PositivesRunner extends BaseRunner {
    @Override
    public @NonNls
    String getCommandName() {
        return "PositiveRefactorings";
    }

    @Override
    Options configureOptionsForCLI() {
        Options options = new Options();
        options.addRequiredOption("input", "inputMappingPath", true, "Path to the input mapping.");
        options.addRequiredOption("output", "outputFilePath", true, "Path to the desired output destination.");

        return options;
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

        Path mappingPath = null;
        try {
            mappingPath = Paths.get(cmdLine.getOptionValue("inputMappingPath"));
        } catch (InvalidPathException e) {
            LOG.error("<inputMappingPath> has to be a valid path.");
            return;
        }


        FileWriter positiveFW = null;
        try {
//            positiveFW = RunnerUtils.makeHeader(cmdLine.getOptionValue("outputFilePath"), featureCount);
            positiveFW = new FileWriter(cmdLine.getOptionValue("outputFilePath"));
            positiveFW.append('[');
        } catch (IOException e) {
            LOG.error("Failed to make header for the output file");
            return;
        }

        RefactoringsExtractor extractor = new PositivesExtractor(positiveFW);

        try {
            runner.runMultipleExtractions(mappingPath, extractor);
        } catch (Exception e) {
            LOG.error("Unexpected error in positive" +
                    " samples' procedure. \n" + e.getMessage());
        }
    }
}
