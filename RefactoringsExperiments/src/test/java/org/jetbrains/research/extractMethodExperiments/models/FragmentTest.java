package org.jetbrains.research.extractMethodExperiments.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class FragmentTest {
    String codeToTest1 =
            "for(int i = 0; i < 100; i++) {\n" +
                    "     if(a>b){\n" +
                    "          i++;\n" +
                    "     } else {\n" +
                    "          j++;\n" +
                    "     }\n" +
                    "}";

    String codeToTest2 = "\n\n{{return i;}}   \n\n   ";


    @Test
    void clearCodeTest() {
        assertEquals(codeToTest1, Fragment.clearCode(codeToTest1), "valid code should remain valid");
        assertEquals("return i;", Fragment.clearCode(codeToTest2), "embedded into braces code be cleared");
    }

    @Test
    void getNestingDepthTest() {
        assertEquals(2, Fragment.getNestingDepth(codeToTest2), "simple nesting depth should be properly calculated");
        assertEquals(2, Fragment.getNestingDepth(codeToTest1), "more difficult nesting depth should be properly calculated");
    }

    @Test
    void getNestingAreaTest() {
        assertEquals(7, Fragment.getNestingArea(codeToTest1), "simple nesting area should be properly calculated");
    }

}