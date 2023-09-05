package de.firemage.autograder.core.check_tests.SystemSpecificLineBreak.code;

public class Test {
    private static final String LINE_BREAK_1 = "\n"; /*# not ok #*/
    private static final String LINE_BREAK_2 = "\r\n"; /*# not ok #*/
    private static final String LINE_BREAK_3 = "\\r\\n"; /*# not ok #*/
    private static final String LINE_BREAK_4 = "\\n"; /*# not ok #*/
    private static final char LINE_BREAK_CHARACTER_1 = '\n'; /*# not ok #*/

    public static void main(String[] args) {}
}
