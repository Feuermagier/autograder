package de.firemage.autograder.core.check_tests.DoNotUseRawTypes.code;

import java.util.*;

public class Test {
    private List l1; /*# not ok #*/
    private List<String> l2; /*# ok #*/
    private Map l3; /*# not ok #*/
    private Map<String, String> l4; /*# ok #*/
    private Set l5; /*# not ok #*/
    private Map<String, Set> l6; /*# not ok #*/
    private Map<String, Set>[] l7; /*# not ok #*/
    private Map<String, Set>[][] l8; /*# not ok #*/
    private Map<ArrayList /*# not ok #*/, HashMap /*# not ok #*/>[][] l9;
    public static void main(String[] args) {
    }

    void example1() {
        List /*# not ok #*/ aList = new ArrayList /*# not ok #*/();
        String s = "Hello World!";
        aList.add(s);
        String c = (String)aList.get(0);
        for (Map.Entry<String, String> entry : l4.entrySet()) {} /*# ok #*/
    }
}
