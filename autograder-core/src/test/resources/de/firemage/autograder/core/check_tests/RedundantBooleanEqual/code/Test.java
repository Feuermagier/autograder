package de.firemage.autograder.core.check_tests.RedundantBooleanEqual.code;

public class Test {
    public static void main(String[] args) {
        if ((args.length == 0) == true) {} /*@ not ok @*/
        if ((args.length == 0) == false) {} /*@ not ok @*/
        if ((args.length == 0) != true) {} /*@ not ok @*/
        if ((args.length == 0) != false) {} /*@ not ok @*/
        if (args.length == 0) {} /*@ ok @*/
    }
}

class TestResolution {
    private static boolean TRUE = true;
    private static boolean FALSE = false;

    void foo(boolean a) {
        if (a == TRUE) {} /*@ not ok @*/
    }
}
