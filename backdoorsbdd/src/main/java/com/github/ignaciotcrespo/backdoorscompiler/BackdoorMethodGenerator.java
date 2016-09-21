package com.github.ignaciotcrespo.backdoorscompiler;

import com.github.ignaciotcrespo.backdoorsapi.BackdoorsContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
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

    MethodSpec createBackdoorMethod(ExecutableElement annotatedMtd, String name, Set<? extends Element> contexts) {
        StringBuilder statement = new StringBuilder()
                .append(ClassName.get(annotatedMtd.getEnclosingElement().asType()))
                .append(".")
                .append(annotatedMtd.getSimpleName())
                .append("(");

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getValidMethodName(name));

        checkParameters(annotatedMtd, statement, builder);

        statement.append(")");

        addModifiers(builder);

        addReturnType(annotatedMtd, statement, builder, contexts);

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

    void addReturnType(ExecutableElement annotatedMtd, StringBuilder statement, MethodSpec.Builder builder, Set<? extends Element> contexts) {
        if (returnsSomething(annotatedMtd)) {
            builder.returns(ClassName.get(annotatedMtd.getReturnType()));
            builder.addStatement("return " + statement);
        } else {
            addReturnVoid(annotatedMtd, statement, builder, contexts);
        }
    }

    private void addReturnVoid(ExecutableElement annotatedMtd, StringBuilder statement, MethodSpec.Builder builder, Set<? extends Element> contexts) {
        builder.addStatement(statement.toString());
        String voidValue = getVoidValue(contexts);
        if(voidValue.trim().length() > 0) {
            builder.addStatement("return \"" + voidValue +"\"");
            builder.returns(TypeName.get(String.class));
        } else {
            builder.returns(ClassName.get(annotatedMtd.getReturnType()));
        }
    }

    private boolean returnsSomething(ExecutableElement annotatedMtd) {
        return annotatedMtd.getReturnType().getKind() != TypeKind.VOID;
    }

    private String getVoidValue(Set<? extends Element> contexts) {
        return contexts.iterator().next().getAnnotation(BackdoorsContext.class).voidValue();
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