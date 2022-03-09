package org.jetbrains.research.extractMethod.core.utils;

import java.io.FileWriter;
import java.io.IOException;

public class WriteUtil {
    public static void writeAuxLocFeatures(String repoFullName, String commitHash, String filePath,
                                           int beginLine, int endLine, FileWriter fileWriter) throws IOException {
        fileWriter.append(repoFullName);
        fileWriter.append(';');
        fileWriter.append(commitHash);
        fileWriter.append(';');
        fileWriter.append(filePath);
        fileWriter.append(';');
        fileWriter.append(String.valueOf(beginLine));
        fileWriter.append(';');
        fileWriter.append(String.valueOf(endLine));
        fileWriter.append(';');
    }

    public static void writeCodeFragment(String code, FileWriter fileWriter) throws IOException {
        fileWriter.append(String.format("\"%s\"", code));
    }
}
