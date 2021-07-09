package org.jetbrains.research.extractMethodExperiments.models;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

class RankEvaluatorTest {
    Fragment toTestFragment;

    @BeforeAll
    void makeFragment() throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get("dummy.java.txt")))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        contentBuilder.toString();
    }

    @Test
    void sLength() {

    }

    @Test
    void sNestDepth() {
    }

    @Test
    void sNestArea() {
    }

    @Test
    void sParam() {
    }

    @Test
    void sCommentsAndBlanks() {
    }

    @Test
    void getScore() {
    }
}