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

    int sec; // Not Ok
    int min; // Not Ok

    int interpret; // Ok
    String interpreters; // Ok
    List<String> validStrings; // Not Ok
    Set<Integer> integerSet; // Not Ok

    int pointer; // Ok
    int pointerValue; // Ok
    int flagInternal; // Ok
    int playerPointer; // Ok

    int maxValue; // Ok
    int minValue; // Ok

    int maximumValue; // Ok
    int minimumValue; // Ok

    int maxNumber; // Ok
    int minNumber; // Ok

    int max_number; // Ok (wrong case, previoulsy resulted in a crash)
}

enum Month {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE, // Ok
    JULY, // Ok
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER
}
