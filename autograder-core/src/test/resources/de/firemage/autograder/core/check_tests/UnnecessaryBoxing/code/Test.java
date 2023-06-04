package de.firemage.autograder.core.check_tests.UnnecessaryBoxing.code;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> value = List.of(); // Ok

        Double aDouble = 1.0d; // Ok
        aDouble = null;

        Double myValue = 3.14d; // Not Ok

        Double potentiallyNull = getDouble(args.length > 0); // Ok
        Double uninitialized; // Ok
    }

    private static Double getDouble(boolean condition) {
        if (condition) {
            return null;
        } else {
            return 1.123d;
        }
    }
}
