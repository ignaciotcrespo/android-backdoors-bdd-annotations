package com.github.ignaciotcrespo.backdoorscompiler;

import com.github.ignaciotcrespo.backdoorsapi.Backdoor;
import com.github.ignaciotcrespo.backdoorsapi.Backdoors;
import com.github.ignaciotcrespo.backdoorsapi.BackdoorsContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Process at compile time all backdoor annotations.
 */
@AutoService(Processor.class)
public class BackdoorsProcessor extends AbstractProcessor {

    private final Messager messager = new Messager();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(BackdoorsContext.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if(annotations.size() == 0){
            return true;
        }

        messager.print("backdoors: process annotations");

        Set<? extends Element> contexts = roundEnv.getElementsAnnotatedWith(BackdoorsContext.class);
        if (contexts != null && contexts.size() > 1) {
            throw new RuntimeException("Not allowed more than 1 @" + BackdoorsContext.class.getSimpleName());
        }
        if (contexts == null || contexts.size() == 0) {
            throw new RuntimeException("No context defined for backdoors, your Application class must include the annotation @" + BackdoorsContext.class.getSimpleName()
            +"\n and your manifest must add the postfix 'Backdoors' to the application name, e.g. <application android:name=\"[YOUR APP CLASS HERE]Backdoors\"");
        }

        String backdoorsApp = contexts != null && contexts.size() == 1 ? contexts.iterator().next().getSimpleName() + "Backdoors" : "BackdoorsApp";
        TypeSpec.Builder builder1 = classBuilder(backdoorsApp)
                .addModifiers(PUBLIC /*, FINAL */);

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Backdoor.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            ExecutableElement annotatedMtd = (ExecutableElement) annotatedElement;
            String name = annotatedMtd.getAnnotation(Backdoor.class).value();

            processBackdoor(builder1, annotatedMtd, name);
        }
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Backdoors.class)) {

            // annotation is only allowed on classes, so we can safely cast here
            ExecutableElement annotatedMtd = (ExecutableElement) annotatedElement;
            Backdoors annotation = annotatedMtd.getAnnotation(Backdoors.class);
            for (String name : annotation.value()) {
                processBackdoor(builder1, annotatedMtd, name);
            }
        }

        builder1.superclass(contexts != null && contexts.size() == 1 ? get(contexts.iterator().next().asType()) :  ClassName.bestGuess("android.app.Application"));
        TypeSpec generatedClass = builder1.build();

        JavaFile.Builder builder = JavaFile.builder(contexts != null && contexts.size() == 1 ? processingEnv.getElementUtils().getPackageOf(contexts.iterator().next()).getQualifiedName().toString() : "nl.ngti", generatedClass);
        JavaFile javaFile = builder.build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void processBackdoor(TypeSpec.Builder builder1, ExecutableElement annotatedMtd, String name) {
        if (!annotatedMtd.getModifiers().contains(STATIC)) {
            throw new RuntimeException("@Backdoor is for static methods only. Check method: " + get(annotatedMtd.getEnclosingElement().asType()) + "." + annotatedMtd.getSimpleName());
        }
        if (!annotatedMtd.getModifiers().contains(PUBLIC)) {
            throw new RuntimeException("@Backdoor is for public methods only. Check method: " + get(annotatedMtd.getEnclosingElement().asType()) + "." + annotatedMtd.getSimpleName());
        }

        messager.print("creating backdoor: "+ name);

        List<? extends VariableElement> params = annotatedMtd.getParameters();

        StringBuilder statement = new StringBuilder()
                .append(get(annotatedMtd.getEnclosingElement().asType()))
                .append(".")
                .append(annotatedMtd.getSimpleName())
                .append("(");

        MethodSpec.Builder builder = methodBuilder(name.replace(' ', '_'));
        for (int i = 0; i < params.size(); i++) {
            VariableElement elem = params.get(i);
            builder.addParameter(get(elem.asType()), elem.getSimpleName().toString());
            if (i > 0) statement.append(", ");
            statement.append(elem.getSimpleName());
        }
        builder
                .addModifiers(PUBLIC /*, STATIC */)
                //.addParameter(get("android.content", "Context"), "context")
                //.addStatement("return new $T(context, $L.class)", intentClass, annotatedClassName)
                .returns(get(annotatedMtd.getReturnType()));
        statement.append(")");
        if (annotatedMtd.getReturnType().getKind() != TypeKind.VOID) {
            builder.addStatement("return " + statement);
        } else {
            builder.addStatement(statement.toString());
        }
        List<? extends TypeMirror> exceptions = annotatedMtd.getThrownTypes();
        for (TypeMirror exc : exceptions) {
            builder.addException(get(exc));
        }
        MethodSpec mtd = builder.build();

        builder1.addMethod(mtd);
    }

}
