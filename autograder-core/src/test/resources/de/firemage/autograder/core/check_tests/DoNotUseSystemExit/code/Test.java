package de.firemage.autograder.core.check_tests.DoNotUseSystemExit.code;

public class Test {
    public static void main(String[] args) {
        System.exit(1); /*# not ok #*/
        System.exit(123); /*# not ok #*/
        int myVar = 5;
        if (true) {
            System.exit(myVar); /*# not ok #*/
        }
    }
}
