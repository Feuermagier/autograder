package de.firemage.autograder.core.check_tests.CompareCharValue.code;

public class Test {
    public static void main(String[] args) {
        char symbol = 'a';
        boolean isValid = (symbol > 47 /*# not ok #*/ && symbol < 58 /*# not ok #*/) || symbol == '*' || symbol == '-' || symbol == '+';
        isValid = (symbol > '/' && symbol < ':') || symbol == '*' || symbol == '-' || symbol == '+'; /*# ok #*/
    }
}
