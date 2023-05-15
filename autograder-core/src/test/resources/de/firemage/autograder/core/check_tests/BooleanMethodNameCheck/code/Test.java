package de.firemage.autograder.core.check_tests.BooleanMethodNameCheck.code;

public class Test {
    public static void main(String[] args) {}

    public boolean getValue() { // Not Ok
        return true;
    }

    public boolean value() { // Ok
        return true;
    }

    public boolean isValue() { // Ok
        return true;
    }
}
