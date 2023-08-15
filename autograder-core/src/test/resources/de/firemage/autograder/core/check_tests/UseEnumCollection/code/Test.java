package de.firemage.autograder.core.check_tests.UseEnumCollection.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        enum Color {
            RED, GREEN, BLUE;
        }

        Map<Color, Integer> rgb = new HashMap<>(); /*# not ok #*/
        Set<Color> colors = new HashSet<>(); /*# not ok #*/
    }
}
