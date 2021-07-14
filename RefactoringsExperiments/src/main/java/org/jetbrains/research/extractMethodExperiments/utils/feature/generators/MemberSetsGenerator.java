package org.jetbrains.research.extractMethodExperiments.utils.feature.generators;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.List;

public class MemberSetsGenerator {
    public MembersSets instanceMembers(Node cur) {
        MembersSets result = new MembersSets();

        if (cur instanceof ClassOrInterfaceDeclaration) {
            ClassOrInterfaceDeclaration decl = (ClassOrInterfaceDeclaration) cur;

            result.total.add(decl.getName().toString());

        }

        if (cur instanceof MethodDeclaration) {
            MethodDeclaration decl = (MethodDeclaration) cur;

            result.methods.add(decl.getName().toString());
            result.total.add(decl.getName().toString());
            return result;
        }

        if (cur instanceof FieldDeclaration) {
            FieldDeclaration decl = (FieldDeclaration) cur;
            List<VariableDeclarator> vars = decl.getVariables();
            if (vars != null) {
                for (VariableDeclarator vd : vars) {
                    if (vd.getName() != null) {
                        result.total.add(vd.getName().toString());
                        result.fields.add(vd.getName().toString());
                    }

                }
            }

            return result;
        }

        for (Node n : cur.getChildNodes()) {
            MembersSets subres = instanceMembers(n);
            result.total.addAll(subres.total);
            result.fields.addAll(subres.fields);
            result.methods.addAll(subres.methods);
        }

        return result;
    }
}
