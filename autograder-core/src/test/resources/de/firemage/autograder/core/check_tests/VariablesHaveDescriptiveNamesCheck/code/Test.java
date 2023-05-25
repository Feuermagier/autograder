package de.firemage.autograder.core.check_tests.VariablesHaveDescriptiveNamesCheck.code;

import java.util.*;
public class Test {
    private static List<String> list; // Ok
    private static List<String> exampleList; // Not Ok
    private static int[] exampleIntArray; // Not Ok
    private static int[] intArray; // Not Ok
    private static String s; // Not Ok
    private static String string; // Ok

    int trafficLight1;
    int trafficLight2; // Not Ok (similar to trafficLight1)
    int trafficLight3; // Not Ok (similar to trafficLight1)

    int result1; // Not Ok (could be result)
}
