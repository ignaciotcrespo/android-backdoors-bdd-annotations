package com.github.ignaciotcrespo.android_backdoorsbdd_annotations.example.utils;

import com.github.ignaciotcrespo.backdoorsapi.Backdoor;
import com.github.ignaciotcrespo.backdoorsapi.Backdoors;

/**
 * Created by crespo on 07/08/16.
 */
public class Example2 {

    @Backdoor("third_backdoor_with_parameters")
    public static void method3(String p1, int p2) {

    }

    @Backdoor("fourth_backdoor_with_parameters_and_return")
    public static String method4(String p1, int p2) {
        return "any string";
    }

    @Backdoors({
            "multiple_backdoors_in_one_method",
            "another_backdoor_for_this_method"
    })
    public static void method5() {

    }

}
