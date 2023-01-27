package de.firemage.autograder.core.check_tests.StringCompareCheck.code;

public class Test {
    public static void main(String[] args) {
        String a = "a";
        String b = "b";
        String c = "c";

        boolean r;
        r = a == b; // Not Ok
        r = a == "a"; // Not Ok
        r = "a" == a; // Not Ok
        r = "a" == "a"; // Not Ok
        r = "a" == null; // Ok
        r = null == "a"; // Ok
        r = a == null; // Ok
        r = null == a; // Ok
        r = a != "c"; // Not Ok
        r = "c" != a; // Not Ok
        r = "c" != "c"; // Not Ok
    }
}
