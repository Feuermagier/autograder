package de.firemage.autograder.core.check_tests.ClosedSetOfValues.code;

import java.util.*;

public class Test {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        switch (SCANNER.nextLine()) { // Not Ok
            case "print":
                System.out.println("a");
                break;
            case "hello":
                System.out.println("b");
                break;
            case "quit":
                System.out.println("c");
                break;
            default:
                System.out.println("default");
        }

        switch (SCANNER.nextLine().toCharArray()[0]) { // Not Ok
            case '0':
                System.out.println("case 0");
                break;
            case '1':
                System.out.println("case 1");
                break;
            case '2':
                System.out.println("case 2");
                break;
            default:
                System.out.println("default");
        }
    }
}

class TestFiniteListing {
    private static final char[] VALID_ARRAY = {'R', 'x', 'X', '|', '\\', '/', '_', '*', ' '}; // Not Ok
    private static final List<Character> VALID_LIST = List.of('R', 'x', 'X', '|', '\\', '/', '_', '*', ' '); // Not Ok
    private static final Set<String> FINITE_SET = Set.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"); // Not Ok
    private static final char[] NOT_ENOUGH_DISTINCT_VALUES_ARRAY = { 'a', 'b', 'b', 'a' }; // Ok (2 distinct values)
    private static final List<Character> NOT_ENOUGH_DISTINCT_VALUES_LIST = List.of('a', 'b', 'b', 'a'); // Ok (2 distinct values)
    private static final Set<String> NOT_ENOUGH_DISTINCT_VALUES_SET = Set.of("monday", "monday", "monday", "monday"); // Ok (1 distinct value)
}
