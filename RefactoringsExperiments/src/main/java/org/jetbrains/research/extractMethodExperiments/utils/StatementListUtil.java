package org.jetbrains.research.extractMethodExperiments.utils;

import com.intellij.psi.PsiStatement;

import java.util.List;

public class StatementListUtil {
    public static String ListToStr(List<PsiStatement> statementList){
        StringBuilder result = new StringBuilder();
        for (PsiStatement statement : statementList) {
            result.append(statement.getText());
            result.append('\n');
        }
        return result.toString().strip();
    }
}
