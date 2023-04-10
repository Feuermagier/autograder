package de.firemage.autograder.core.check_tests.Assert.code;

public class Test {
    public static void main(String[] args) {
        l1: {
            int x = 4;
        }
        label: if (true) {
        }

        l2: while (true) {
            break l2;
        }
    }
}
