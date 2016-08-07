package com.github.ignaciotcrespo.backdoorscompiler;

import com.github.ignaciotcrespo.backdoorsapi.BackdoorsContext;
import com.google.auto.service.AutoService;

import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.SourceVersion.latestSupported;

/**
 * Process at compile time all backdoor annotations.
 */
@AutoService(Processor.class)
public class BackdoorsProcessor extends AbstractProcessor {

    private final Messager messager = new Messager();
    private final BackdoorsClassGenerator mBackdoorsClassGenerator = new BackdoorsClassGenerator();
    private final BackdoorsAnalyzer mBackdoorsAnalyzer = new BackdoorsAnalyzer(mBackdoorsClassGenerator, messager);

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

        if (annotations.size() == 0) {
            return true;
        }

        messager.print("backdoors: process annotations");

        Set<? extends Element> contexts = roundEnv.getElementsAnnotatedWith(BackdoorsContext.class);

        assertValidContext(contexts);

        mBackdoorsClassGenerator.prepareClass(contexts);

        mBackdoorsAnalyzer.processBackdoors(roundEnv);

        mBackdoorsClassGenerator.saveClass(contexts, processingEnv);

        return true;
    }

    private void assertValidContext(Set<? extends Element> contexts) {
        if (contexts != null && contexts.size() > 1) {
            throw new RuntimeException("Not allowed more than 1 @" + BackdoorsContext.class.getSimpleName());
        }
        if (contexts == null || contexts.size() == 0) {
            throw new RuntimeException("No context defined for backdoors, your Application class must include the annotation @" + BackdoorsContext.class.getSimpleName()
                    + "\n and your manifest must add the postfix 'Backdoors' to the application name, e.g. <application android:name=\"[YOUR APP CLASS HERE]Backdoors\"");
        }
    }


}
