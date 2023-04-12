public class Test {
    private String noRegex = "Should we do this? I guess we shouldn't! f*ck you!"; // ok
    private String regex1 = "(foo)* [bar]+ x? x?"; // not ok
    private String regex2 = "(?<g1>foo)"; // not ok
    private String regex3 = "\\d*.\\d*"; // not ok
    private String simpleRegex = "\\d*"; // ok
    private String invalidRegex = "(foo* [bar]+ x? x?"; // ok
}
