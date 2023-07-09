package de.firemage.autograder.core.check_tests.Assert.code;

public class Test {
    public static void main(String[] args) {
        assert true; /*@ not ok @*/
        int x = 4;
        assert false: "Hi"; /*@ not ok @*/
        if (true) {
            assert true != false; /*@ not ok @*/
        }
    }
}
