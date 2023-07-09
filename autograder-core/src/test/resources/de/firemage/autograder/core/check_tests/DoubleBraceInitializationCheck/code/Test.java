package de.firemage.autograder.core.check_tests.DoubleBraceInitializationCheck.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        Set<String> countries = new HashSet<String>() { /*@ not ok @*/
            {
                add("Germany");
            }
        };
    }
}
