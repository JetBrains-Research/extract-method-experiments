package org.jetbrains.research.extractMethodExperiments.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;

public class PsiUtil {
    public static int getNumberOfLine(PsiFile file, int offset) {
        FileViewProvider fileViewProvider = file.getViewProvider();
        Document document = fileViewProvider.getDocument();
        return document != null ? document.getLineNumber(offset) + 1 : 0;
    }
}
