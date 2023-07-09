package de.firemage.autograder.core.check_tests.VariablesHaveDescriptiveNamesCheck.code;

import java.util.*;
public class Test {
    private static List<String> list; /*@ ok @*/
    private static List<String> exampleList; /*@ not ok @*/
    private static int[] exampleIntArray; /*@ not ok @*/
    private static int[] intArray; /*@ not ok @*/
    private static String s; /*@ not ok @*/
    private static String string; /*@ ok @*/
    int trafficLight1;
    int trafficLight2; /*@ not ok; similar to trafficLight1 @*/
    int trafficLight3; /*@ not ok; similar to trafficLight1 @*/

    int result1; /*@ not ok; could be result @*/

    int sec; /*@ not ok @*/
    int min; /*@ not ok @*/
    int interpret; /*@ ok @*/
    String interpreters; /*@ ok @*/
    List<String> validStrings; /*@ not ok @*/
    Set<Integer> integerSet; /*@ not ok @*/
    int pointer; /*@ ok @*/
    int pointerValue; /*@ ok @*/
    int flagInternal; /*@ ok @*/
    int playerPointer; /*@ ok @*/
    int maxValue; /*@ ok @*/
    int minValue; /*@ ok @*/
    int maximumValue; /*@ ok @*/
    int minimumValue; /*@ ok @*/
    int maxNumber; /*@ ok @*/
    int minNumber; /*@ ok @*/
    int max_number; /*@ ok; wrong case, previoulsy resulted in a crash @*/

    private void test() {
        Test[] tests = new Test[5];
        for (Test test : tests) { } /*@ ok @*/
        for (char c: "abc".toCharArray()) { } /*@ ok @*/
    }

    String datePattern; /*@ ok @*/
    String namePattern; /*@ ok @*/
    String timePattern; /*@ ok @*/
}

enum Month {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE, /*@ ok @*/
    JULY, /*@ ok @*/
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER
}

// crashes if implicit identifiers are not ignored
enum BuildingType {
    HOUSE("H") {
        @Override
        public boolean isHousing() {
            return true;
        }
    };

    private final String symbol;

    BuildingType(String symbol) {
        this.symbol = symbol;
    }

    public abstract boolean isHousing();
}
