package de.firemage.autograder.core.check_tests.ConstantsHaveDescriptiveNamesCheck.code;

public class Test {
    private static final int ZERO = 0; // Not Ok
    private static final int ONE = 1; // Not Ok
    private static final int TWO = 2; // Not Ok
    private static final String ERROR = ""; // Not Ok
    private static final String REGEX = ""; // Not Ok

    private static final int VALUE_A = 1; // Ok

    private static final Object d = null; // Not Ok
    private static final boolean TRUE = true; // Not Ok
    private static final boolean FALSE = false; // Not Ok
    private static final String UP = "up"; // Not Ok
    private static final String DOWN = "down"; // Not Ok
    private static final String DEBUG_PATH = "debug-path"; // Not Ok
    private static final String LEFT = "left"; // Not Ok
    private static final String RIGHT = "right"; // Not Ok

    private static final String SPLIT_COLON = ":"; // Not Ok
    private static final String SPLIT_COMMA = ","; // Not Ok
    private static final String SPLIT_ARROW = "-->"; // Not OK
}
