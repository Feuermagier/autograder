package de.firemage.autograder.core.check_tests.CompareCharValue.code;

public class Test {
    public static void main(String[] args) {
        char symbol = 'a';
        boolean isValid = (symbol > 47 && symbol < 58) || symbol == '*' || symbol == '-' || symbol == '+'; // Not Ok
        isValid = (symbol > '/' && symbol < ':') || symbol == '*' || symbol == '-' || symbol == '+'; // Ok
    }
}
