package de.firemage.autograder.core.check_tests.AvoidRecompilingRegex.code;

import java.util.regex.Pattern;

public class Test {
    private static final String REGEX = "[A-Z][a-z]+"; // Not Ok
    private static final String REGEX_2 = "[A-Z][a-z]+"; // Ok

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile(REGEX);
        Pattern pattern2 = Pattern.compile(REGEX_2);
        System.out.println(REGEX_2);
    }
}
