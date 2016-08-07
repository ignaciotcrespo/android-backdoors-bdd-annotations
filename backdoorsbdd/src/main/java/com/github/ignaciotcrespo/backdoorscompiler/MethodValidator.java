package com.github.ignaciotcrespo.backdoorscompiler;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * All validations for the annotated methods.
 */
public class MethodValidator {
    public MethodValidator() {
    }

    void assertValidMethod(ExecutableElement annotatedMtd) {
        checkStatic(annotatedMtd);
        checkPublic(annotatedMtd);
    }

    void checkPublic(ExecutableElement annotatedMtd) {
        if (!annotatedMtd.getModifiers().contains(PUBLIC)) {
            throw new RuntimeException("@Backdoor is for public methods only. Check method: " + ClassName.get(annotatedMtd.getEnclosingElement().asType()) + "." + annotatedMtd.getSimpleName());
        }
    }

    void checkStatic(ExecutableElement annotatedMtd) {
        if (!annotatedMtd.getModifiers().contains(STATIC)) {
            throw new RuntimeException("@Backdoor is for static methods only. Check method: " + ClassName.get(annotatedMtd.getEnclosingElement().asType()) + "." + annotatedMtd.getSimpleName());
        }
    }
}