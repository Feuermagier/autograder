package de.firemage.autograder.core.check_tests.Regex.code;

import java.util.regex.Pattern;

public class Test {
    private String noRegex = "Should we do this? I guess we shouldn't! f*ck you!";
    private String regex1 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private String regex2 = "(?<g1>foo)"; /*# not ok #*/
    private String regex3 = "^[a-z]+(?: \\S+)?$"; /*# not ok #*/
    private String regex4 = "^(?<start>\\d+)-->(?<end>\\d+):(?<length>\\d+)m,(?<type>\\d+)x,(?<velocity>\\d+)max$"; /*# not ok #*/
    private String regex5 = "^(?<identifier>\\d+),(?<street>\\d+),(?<velocity>\\d+),(?<acceleration>\\d+)$"; /*# not ok #*/
    private String simpleRegex1 = "\\d*.\\d*";
    private String simpleRegex2 = "\\d*";
    private String simpleRegex3 = "^[a-z]+";
    private String invalidRegex = "(foo* [bar]+ x? x?";

    /**
     * This comment is explaining how the regex works...
     */
    private static final String COMPLICATED_REGEX_1 = "^(?<identifier>\\d+),(?<street>\\d+),(?<velocity>\\d+),(?<acceleration>\\d+)$";
    // Inline comments should be acceptable as well
    private static final String COMPLICATED_REGEX_2 = "^(?<identifier>\\d+),(?<street>\\d+),(?<velocity>\\d+),(?<acceleration>\\d+)$";

    private static final String FORMAT_STRING_1 = "coordinate (%s, %s) is invalid!";
    private static final String FORMAT_STRING_2 = "coordinate (%s, %s)\n is invalid?\n";

    private void foo() {
        Pattern pattern = Pattern.compile(regex1);
        pattern = Pattern.compile(regex2);
        pattern = Pattern.compile(regex3);
        pattern = Pattern.compile(regex4);
        pattern = Pattern.compile(regex5);
        pattern = Pattern.compile(simpleRegex1);
        pattern = Pattern.compile(simpleRegex2);
        pattern = Pattern.compile(simpleRegex3);
        pattern = Pattern.compile(invalidRegex);
        pattern = Pattern.compile(COMPLICATED_REGEX_1);
        pattern = Pattern.compile(COMPLICATED_REGEX_2);
        pattern = Pattern.compile(FORMAT_STRING_1);
        pattern = Pattern.compile(FORMAT_STRING_2);
    }
}

// test that context of regex is considered
class RegexContext {
    private static final String DEFINITELY_REGEX_1 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private static final String DEFINITELY_REGEX_2 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private static final String DEFINITELY_REGEX_3 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private static final String DEFINITELY_REGEX_4 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private static final String DEFINITELY_REGEX_5 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private static final String DEFINITELY_REGEX_6 = "(foo)* [bar]+ x? x?"; /*# not ok #*/
    private static final String UNUSED_REGEX = "(foo)* [bar]+ x? x?"; /*# ok #*/
    private static final String NOT_USED_AS_REGEX = "(foo)* [bar]+ x? x?"; /*# ok #*/

    void foo() {
        boolean matches = Pattern.matches(DEFINITELY_REGEX_1, "foo bar x");
        matches = "foo bar x".matches(DEFINITELY_REGEX_2);
        String f = "foo bar x".replaceAll(DEFINITELY_REGEX_3, "foo bar x");
        f = "foo bar x".replaceFirst(DEFINITELY_REGEX_4, "foo bar x");
        String[] parts = "foo bar x".split(DEFINITELY_REGEX_5);
        parts = "foo bar x".split(DEFINITELY_REGEX_6, -1);

        System.out.println(NOT_USED_AS_REGEX);
    }
}


class B {
    B(String string) {}
}

class A extends B {
    A() {
        super("abc123"); // did result in crash
    }
}
