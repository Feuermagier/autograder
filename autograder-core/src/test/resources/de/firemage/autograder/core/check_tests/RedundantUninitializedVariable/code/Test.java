package de.firemage.autograder.core.check_tests.RedundantUninitializedVariable.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {}

    private static void motivation() {
        int i; /*@ not ok @*/
        i = 1;
    }

    private static void conditional01(boolean isTrue) {
        int i; /*@ ok; for now @*/

        if (isTrue) {
            i = 3;
        } else {
            i = 2;
        }
    }

    private static void assignmentInSubBlock(boolean isTrue) {
        int i; /*@ not ok @*/
        {
            i = 3;
        }
    }
}

record MyRecord(String string) {
    public MyRecord(String string, String value2) {
        this(value2);

        String string2; /*@ not ok @*/
        string2 = "string2";
    }

    MyRecord {
        // compact constructor
        string = "abc123";
    }
}

class MoreTests {
    void e() {
        int i; /*@ ok @*/
        int j; /*@ false negative; dont want to bother detecting this @*/
        for (j = 0; j < 5; j++) {
            System.out.println(j);
        }
    }
}
