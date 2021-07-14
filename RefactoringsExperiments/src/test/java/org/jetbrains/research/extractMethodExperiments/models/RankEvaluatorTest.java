package org.jetbrains.research.extractMethodExperiments.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RankEvaluatorTest {

    String remainderToTest = "public static int countSmth(int a, int b) {\n" +
            "    a++;\n" +
            "    callToExtractedMethod();\n" +
            "}";

    String fragmentToTest1 =
            "for(int i = 0; i < 100; i++) {\n" +
                    "     if(a>b){\n" +
                    "          i++;\n" +
                    "     } else {\n" +
                    "          j++;\n" +
                    "     }\n" +
                    "}";


    RankEvaluator rankerToTest = new RankEvaluator(fragmentToTest1, remainderToTest, 3, 14);

    @Test
    void sLengthTest() {
        assertEquals(0.4, rankerToTest.sLength());
    }

    @Test
    void sNestDepthTest() {
        assertEquals(1, rankerToTest.sNestDepth());
    }

    @Test
    void sNestAreaTest() {
        assertEquals(3, rankerToTest.sNestArea());
    }

    @Test
    void sParamTest() {
        assertEquals(0, rankerToTest.sParam());
    }

    @Test
    void sCommentsAndBlanks() {
        assertEquals(0, rankerToTest.sParam());
    }
}