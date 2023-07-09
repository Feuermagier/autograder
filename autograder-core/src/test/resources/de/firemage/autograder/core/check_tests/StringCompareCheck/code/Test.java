package de.firemage.autograder.core.check_tests.StringCompareCheck.code;

public class Test {
    public static void main(String[] args) {
        String a = "a";
        String b = "b";
        String c = "c";

        boolean r;
        r = a == b; /*@ not ok @*/
        r = a == "a"; /*@ not ok @*/
        r = "a" == a; /*@ not ok @*/
        r = "a" == "a"; /*@ not ok @*/
        r = "a" == null; /*@ ok @*/
        r = null == "a"; /*@ ok @*/
        r = a == null; /*@ ok @*/
        r = null == a; /*@ ok @*/
        r = a != "c"; /*@ not ok @*/
        r = "c" != a; /*@ not ok @*/
        r = "c" != "c"; /*@ not ok @*/
    }
}
