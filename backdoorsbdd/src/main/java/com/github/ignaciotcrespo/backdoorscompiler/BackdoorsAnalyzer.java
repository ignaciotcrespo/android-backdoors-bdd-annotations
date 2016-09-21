package com.github.ignaciotcrespo.backdoorscompiler;

import com.github.ignaciotcrespo.backdoorsapi.Backdoor;
import com.github.ignaciotcrespo.backdoorsapi.Backdoors;
import com.squareup.javapoet.MethodSpec;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Analyze backdoor annotations and delegate to {@link BackdoorMethodGenerator} to create the code.
 */
public class BackdoorsAnalyzer {
    private final BackdoorMethodGenerator mBackdoorMethodGenerator = new BackdoorMethodGenerator();
    private final MethodValidator mMethodValidator = new MethodValidator();
    private final BackdoorsClassGenerator backdoorsClassGenerator;
    private final Messager messager;

    public BackdoorsAnalyzer(BackdoorsClassGenerator backdoorsClassGenerator, Messager messager) {
        this.backdoorsClassGenerator = backdoorsClassGenerator;
        this.messager = messager;
    }

    void processBackdoors(RoundEnvironment roundEnv, Set<? extends Element> contexts) {
        processSingleBackdoors(roundEnv, contexts);
        processMultipleBackdoors(roundEnv, contexts);
    }

    void processMultipleBackdoors(RoundEnvironment roundEnv, Set<? extends Element> contexts) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Backdoors.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            ExecutableElement annotatedMtd = (ExecutableElement) annotatedElement;
            Backdoors annotation = annotatedMtd.getAnnotation(Backdoors.class);
            for (String name : annotation.value()) {
                processBackdoor(annotatedMtd, name, contexts);
            }
        }
    }

    void processSingleBackdoors(RoundEnvironment roundEnv, Set<? extends Element> contexts) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Backdoor.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            ExecutableElement annotatedMtd = (ExecutableElement) annotatedElement;
            String name = annotatedMtd.getAnnotation(Backdoor.class).value();

            processBackdoor(annotatedMtd, name, contexts);
        }
    }

    void processBackdoor(ExecutableElement annotatedMtd, String name, Set<? extends Element> contexts) {
        mMethodValidator.assertValidMethod(annotatedMtd);

        messager.print("creating backdoor: " + name);

        MethodSpec mtd = mBackdoorMethodGenerator.createBackdoorMethod(annotatedMtd, name, contexts);

        backdoorsClassGenerator.addMethod(mtd);
    }

}