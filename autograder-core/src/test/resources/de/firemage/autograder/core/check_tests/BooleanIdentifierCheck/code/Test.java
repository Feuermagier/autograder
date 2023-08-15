package de.firemage.autograder.core.check_tests.BooleanIdentifierCheck.code;

public class Test {
    public static void main(String[] args) {}

    public boolean getValue() { /*# not ok #*/
        return true;
    }

    public boolean value() { /*# ok #*/
        return true;
    }

    public boolean isValue() { /*# ok #*/
        return true;
    }
}
