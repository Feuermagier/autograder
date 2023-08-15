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
}
