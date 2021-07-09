package org.jetbrains.research.extractMethodExperiments.utils;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.VoidType;

import java.io.PrintWriter;
import java.util.List;

public class MethodDataExtractor {
    public static void extractParamsCount(MethodDeclaration md, PrintWriter out) {
        List<Parameter> params = md.getParameters();
        if (params != null) {
            OutputUtils.printLn("PARAMS COUNT: " + params.size(), out);
        }
    }

    public static void isVoidMethod(MethodDeclaration md, PrintWriter out) {
        OutputUtils.printLn("IS VOID METHOD: " + (md.getType() instanceof VoidType), out);
    }
}
