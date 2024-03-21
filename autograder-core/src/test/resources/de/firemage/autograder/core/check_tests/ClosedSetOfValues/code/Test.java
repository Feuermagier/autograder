package de.firemage.autograder.core.check_tests.ClosedSetOfValues.code;

import java.util.*;

public class Test {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        switch (SCANNER.nextLine()) { /*# not ok #*/
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

        switch (SCANNER.nextLine().toCharArray()[0]) { /*# not ok #*/
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
    private static final char[] VALID_ARRAY = {'R', 'x', 'X', '|', '\\', '/', '_', '*', ' '}; /*# not ok #*/
    private static final List<Character> VALID_LIST = List.of('R', 'x', 'X', '|', '\\', '/', '_', '*', ' '); /*# not ok #*/
    private static final Set<String> FINITE_SET = Set.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"); /*# not ok #*/
    private static final char[] NOT_ENOUGH_DISTINCT_VALUES_ARRAY = { 'a', 'b', 'b', 'a' }; /*# ok; 2 distinct values #*/
    private static final List<Character> NOT_ENOUGH_DISTINCT_VALUES_LIST = List.of('a', 'b', 'b', 'a'); /*# ok; 2 distinct values #*/
    private static final Set<String> NOT_ENOUGH_DISTINCT_VALUES_SET = Set.of("monday", "monday", "monday", "monday"); /*# ok; 1 distinct value #*/
}

enum Color {
    RED,
    GREEN,
    BLUE,
    YELLOW;

    public static Color fromString(String value) {
        switch (value) { /*# ok #*/
            case "red":
                return RED;
            case "green":
                return GREEN;
            case "blue":
                return BLUE;
            case "yellow":
                return YELLOW;
            default:
                throw new IllegalArgumentException("Unknown color: " + value);
        }
    }
}

class ClosedSetMethods {
    public String getDayOfWeek(int day) { /*# not ok #*/
        if (day == 1) {
            return "monday";
        }

        if (day == 2) {
            return "tuesday";
        }

        if (day == 3) {
            return "wednesday";
        }

        if (day == 4) {
            return "thursday";
        }

        if (day == 5) {
            return "friday";
        }

        if (day == 6) {
            return "saturday";
        }

        if (day == 7) {
            return "sunday";
        }

        throw new IllegalArgumentException("Invalid day: " + day);
    }

    public String getDayOfWeek2(int day) {
        if (day == 1) {
            return "monday";
        }

        if (day == 2) {
            return "tuesday";
        }

        if (day == 3) {
            return "wednesday";
        }

        if (day == 4) {
            return "thursday";
        }

        if (day == 5) {
            return "friday";
        }

        if (day == 6) {
            return "saturday";
        }

        if (day == 7) {
            return "sunday";
        }

        return null;
    }
}

class EnumMappingCrash {
    public static Color mapColor(String string) {
        return switch (string) { /*# not ok #*/
            case "red" -> Color.RED;
            case "green" -> Color.GREEN;
            case "blue" -> Color.BLUE;
            case "yellow" -> null; // this triggers the crash
            default -> null;
        };
    }
}

class LocalVariable {
    public static void foo() {
        String[] godFavors = {"HW", "IR", "MW", "TS", "TT", "VB", "GHW", "GIR", "GMW", "GTS", "GTT", "GVB"}; //# Not ok
    }
}
