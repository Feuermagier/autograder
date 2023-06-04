package de.firemage.autograder.core.check_tests.RedundantBooleanEqual.code;

public class Test {
    public static void main(String[] args) {
        if ((args.length == 0) == true) {} // Not Ok

        if ((args.length == 0) == false) {} // Not Ok

        if ((args.length == 0) != true) {} // Not Ok

        if ((args.length == 0) != false) {} // Not Ok

        if (args.length == 0) {} // Ok

    }
}

class TestResolution {
    private static boolean TRUE = true;
    private static boolean FALSE = false;

    void foo(boolean a) {
        if (a == TRUE) {} // Not Ok
    }
}
