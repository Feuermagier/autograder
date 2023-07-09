package de.firemage.autograder.core.check_tests.Assert.code;

public class Test {
    public static void main(String[] args) {
        l1: { /*@ not ok @*/
            int x = 4;
        }
        label: if (true) { /*@ not ok @*/
        }

        l2: while (true) { /*@ not ok @*/
            break l2;
        }
    }
}
