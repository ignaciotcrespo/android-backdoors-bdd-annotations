package com.github.ignaciotcrespo.android_backdoorsbdd_annotations.example.utils;

import com.github.ignaciotcrespo.backdoorsapi.Backdoor;

/**
 * Created by crespo on 07/08/16.
 */
public class Example1 {

    @Backdoor("first_backdoor_method1")
    public static void method1(){

    }

    @Backdoor("second backdoor method2 with spaces")
    public static void method2(){

    }

}
