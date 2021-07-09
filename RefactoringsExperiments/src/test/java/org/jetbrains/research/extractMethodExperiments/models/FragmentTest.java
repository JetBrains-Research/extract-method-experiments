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

    @Test
    void clearCode() throws Exception {
        assertEquals(Fragment.clearCode(codeToTest1), codeToTest1, "valid code should remain valid");
    }
}