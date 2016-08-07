package com.github.ignaciotcrespo.backdoorscompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import static javax.lang.model.element.Modifier.PUBLIC;

public class BackdoorsClassGenerator {
    private static final String POSTFIX = "Backdoors";
    private TypeSpec.Builder mClassBuilder;

    void prepareClass(Set<? extends Element> contexts) {
        String backdoorsApp = contexts.iterator().next().getSimpleName() + POSTFIX;
        mClassBuilder = TypeSpec.classBuilder(backdoorsApp).addModifiers(PUBLIC);
    }

    void saveClass(Set<? extends Element> contexts, ProcessingEnvironment processingEnv) {
        mClassBuilder.superclass(ClassName.get(contexts.iterator().next().asType()));
        TypeSpec generatedClass = mClassBuilder.build();

        JavaFile.Builder builder = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(contexts.iterator().next()).getQualifiedName().toString(), generatedClass);
        JavaFile javaFile = builder.build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMethod(MethodSpec methodSpec) {
        mClassBuilder.addMethod(methodSpec);
    }
}