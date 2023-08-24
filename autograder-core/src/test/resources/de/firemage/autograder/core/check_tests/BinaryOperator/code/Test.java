package de.firemage.autograder.core.check_tests.BinaryOperator.code;

public class Test {
    public static void main(String[] args) {}

    public boolean foo(int a, int b) {
        return a == 0 | (b == 1 & a == 3); /*# not ok #*/
    }
}
