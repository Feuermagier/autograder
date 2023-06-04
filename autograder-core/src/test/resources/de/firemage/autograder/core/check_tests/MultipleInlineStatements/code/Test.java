package de.firemage.autograder.core.check_tests.MultipleInlineStatements.code;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6);

        for (int i = 0; i < list.size(); i++) { // Ok
            System.out.println(i);
        }

        for (int i = 0, j = 0; i < list.size(); i++, j++) {} // Not Ok

        int a, b, c = 0; // Not Ok
        a = b = c = 5; // Not Ok
    }
}
