package com.github.ignaciotcrespo.backdoorscompiler;

import com.github.ignaciotcrespo.backdoorsapi.Backdoor;
import com.github.ignaciotcrespo.backdoorsapi.Backdoors;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

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

    void processBackdoors(RoundEnvironment roundEnv) {
        processSingleBackdoors(roundEnv);
        processMultipleBackdoors(roundEnv);
    }

    void processMultipleBackdoors(RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Backdoors.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            ExecutableElement annotatedMtd = (ExecutableElement) annotatedElement;
            Backdoors annotation = annotatedMtd.getAnnotation(Backdoors.class);
            for (String name : annotation.value()) {
                processBackdoor(annotatedMtd, name);
            }
        }
    }

    void processSingleBackdoors(RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Backdoor.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            ExecutableElement annotatedMtd = (ExecutableElement) annotatedElement;
            String name = annotatedMtd.getAnnotation(Backdoor.class).value();

            processBackdoor(annotatedMtd, name);
        }
    }

    void processBackdoor(ExecutableElement annotatedMtd, String name) {
        mMethodValidator.assertValidMethod(annotatedMtd);

        messager.print("creating backdoor: " + name);

        MethodSpec mtd = mBackdoorMethodGenerator.createBackdoorMethod(annotatedMtd, name);

        backdoorsClassGenerator.addMethod(mtd);
    }

}