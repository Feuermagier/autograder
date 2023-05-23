package de.firemage.autograder.core.check_tests.PrintStackTraceCheck.code;

public class Test {
    public static void main(String[] args) {
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace(); // Not Ok
        }

        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println(e); // Ok
        }
    }
}
