package de.firemage.autograder.core.check_tests.EmptyCatchCheck.code;

public class Test {
    public static void main(String[] args) {
        try {
            System.out.println("Hello World!");
        } catch (Exception e) { // Not Ok
            // effectively empty
        }
    }
}
