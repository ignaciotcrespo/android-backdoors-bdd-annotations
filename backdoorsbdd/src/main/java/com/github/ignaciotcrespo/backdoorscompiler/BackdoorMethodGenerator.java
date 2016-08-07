package com.github.ignaciotcrespo.backdoorscompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Creates a method for each backdoor annotation.
 */
public class BackdoorMethodGenerator {
    public BackdoorMethodGenerator() {
    }

    MethodSpec createBackdoorMethod(ExecutableElement annotatedMtd, String name) {
        StringBuilder statement = new StringBuilder()
                .append(ClassName.get(annotatedMtd.getEnclosingElement().asType()))
                .append(".")
                .append(annotatedMtd.getSimpleName())
                .append("(");

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getValidMethodName(name));

        checkParameters(annotatedMtd, statement, builder);

        statement.append(")");

        addModifiers(builder);

        addReturnType(annotatedMtd, statement, builder);

        addExceptions(annotatedMtd, builder);

        return builder.build();
    }

    void addModifiers(MethodSpec.Builder builder) {
        builder.addModifiers(PUBLIC);
    }

    String getValidMethodName(String name) {
        return name.replace(' ', '_');
    }

    void addExceptions(ExecutableElement annotatedMtd, MethodSpec.Builder builder) {
        List<? extends TypeMirror> exceptions = annotatedMtd.getThrownTypes();
        for (TypeMirror exc : exceptions) {
            builder.addException(ClassName.get(exc));
        }
    }

    void addReturnType(ExecutableElement annotatedMtd, StringBuilder statement, MethodSpec.Builder builder) {
        builder.returns(ClassName.get(annotatedMtd.getReturnType()));
        if (annotatedMtd.getReturnType().getKind() != TypeKind.VOID) {
            builder.addStatement("return " + statement);
        } else {
            builder.addStatement(statement.toString());
        }
    }

    void checkParameters(ExecutableElement annotatedMtd, StringBuilder statement, MethodSpec.Builder builder) {
        List<? extends VariableElement> params = annotatedMtd.getParameters();
        for (int i = 0; i < params.size(); i++) {
            VariableElement elem = params.get(i);
            builder.addParameter(ClassName.get(elem.asType()), elem.getSimpleName().toString());
            if (i > 0) statement.append(", ");
            statement.append(elem.getSimpleName());
        }
    }
}