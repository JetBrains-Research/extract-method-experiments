package org.jetbrains.research.extractMethodsReloaded.utils;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLType;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static String calculateSignature(UMLOperation operation) {
        StringBuilder builder = new StringBuilder();
        List<String> parameterTypeList = new ArrayList<>();
        for (UMLType type : operation.getParameterTypeList()) {
            boolean add = parameterTypeList.add(type.toString());
        }

        builder.append(operation.getClassName())
                .append(".")
                .append(calculateSignatureWithoutClassName(operation.getName(), parameterTypeList));
        return builder.toString();
    }

    public static String calculateSignature(PsiMethod method) {
        StringBuilder signature = new StringBuilder(method.getName());
        signature = new StringBuilder(method.getContainingClass().getQualifiedName() + "." + signature + "(");
        PsiParameterList parameterList = method.getParameterList();
        int parametersCount = parameterList.getParametersCount();

        for (int i = 0; i < parametersCount; i++) {
            if (i != parametersCount - 1) {
                signature.append(parameterList.getParameter(i).getType().getPresentableText()).append(", ");
            } else {
                signature.append(parameterList.getParameter(i).getType().getPresentableText());
            }
        }
        signature.append(")");
        return signature.toString();
    }

    public static String calculateSignatureWithoutClassName(String operationName, List<String> parameterTypeList) {
        StringBuilder builder = new StringBuilder();
        builder.append(operationName)
                .append("(");

        parameterTypeList.forEach(x -> builder.append(x).append(", "));

        if (parameterTypeList.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
            builder.deleteCharAt(builder.length() - 1);
        }

        builder.append(")");
        return builder.toString();
    }
}
