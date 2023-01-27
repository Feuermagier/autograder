package de.firemage.autograder.core.check_tests.DoNotUseRawTypes.code;

import java.util.*;
// placeholder for future imports to not destroy the line numbers
// placeholder for future imports to not destroy the line numbers
// placeholder for future imports to not destroy the line numbers

public class Test {
    private List l1; // Not Ok
    private List<String> l2; // Ok
    private Map l3; // Not Ok
    private Map<String, String> l4; // Ok
    private Set l5; // Not Ok
    private Map<String, Set> l6; // Not Ok
    private Map<String, Set>[] l7; // Not Ok
    private Map<String, Set>[][] l8; // Not Ok
    private Map<ArrayList, HashMap>[][] l9; // Not Ok

    public static void main(String[] args) {
    }

    void example1() {
        List aList = new ArrayList(); // Not OK
        String s = "Hello World!";
        aList.add(s);
        String c = (String)aList.get(0);
    }
}
