package com.github.ignaciotcrespo.backdoorsapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by crespo on 05/08/16.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface BackdoorsContext {

    /**
     * In case a method returns a void, the void is replaced by this value.
     * Why we need this? It seems calabash executes the void backdoors asynchronously,
     * using a return value it executes the backdoor synchronously
     * @return the value to return for all void methods
     */
    String voidValue() default "";
}
