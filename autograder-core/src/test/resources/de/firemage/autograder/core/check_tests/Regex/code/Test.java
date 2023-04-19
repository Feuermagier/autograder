public class Test {
    private String noRegex = "Should we do this? I guess we shouldn't! f*ck you!"; // ok
    private String regex1 = "(foo)* [bar]+ x? x?"; // not ok
    private String regex2 = "(?<g1>foo)"; // not ok
    private String regex3 = "^[a-z]+(?: \\S+)?$"; // not ok
    private String regex4 = "^(?<start>\\d+)-->(?<end>\\d+):(?<length>\\d+)m,(?<type>\\d+)x,(?<velocity>\\d+)max$"; // not ok
    private String regex5 = "^(?<identifier>\\d+),(?<street>\\d+),(?<velocity>\\d+),(?<acceleration>\\d+)$"; // not ok
    private String simpleRegex1 = "\\d*.\\d*"; // ok
    private String simpleRegex2 = "\\d*"; // ok
    private String simpleRegex3 = "^[a-z]+"; // ok
    private String invalidRegex = "(foo* [bar]+ x? x?"; // ok
}
