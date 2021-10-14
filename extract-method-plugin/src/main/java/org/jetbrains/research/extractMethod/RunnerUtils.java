package org.jetbrains.research.extractMethod;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.jetbrains.research.extractMethod.metrics.features.Feature;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RunnerUtils {
    /**
     * Creates the output directory to avoid 'FileNotFoundException' later
     */
    public static void configureOutput(CommandLine cmdLine) throws MissingArgumentException, IOException {
        if (cmdLine.hasOption("outputFilePath")) {
            Files.createDirectories(Paths.get(cmdLine.getOptionValue("outputFilePath")).getParent());
        } else {
            throw new MissingArgumentException("Missing <outFilePath>.");
        }
    }

    public static FileWriter makePositiveHeader(String filePath, int featureCount) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (int i = 0; i < featureCount; i++) {
            fw.append(Feature.fromId(i).getName());
            fw.append(';');
        }
        fw.append("RepositoryName");
        fw.append("\n");
        return fw;
    }

    public static FileWriter makeNegativeHeader(String filePath, int featureCount) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        for (int i = 0; i < featureCount; i++) {
            fw.append(Feature.fromId(i).getName());
            fw.append(';');
        }
        fw.append("RepositoryName");
        fw.append(";Score\n");
        return fw;
    }
}
