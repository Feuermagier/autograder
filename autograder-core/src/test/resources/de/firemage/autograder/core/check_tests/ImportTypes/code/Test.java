package de.firemage.autograder.core.check_tests.ImportTypes.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        java.lang.String string = "Hello World"; // Not Ok - import java.lang.String
    }
}

class TestInnerClass {
    static class A { static class B {} }

    void testKnown() {
        A a = new A(); // Ok
        A.B b = new A.B(); // Ok
    }

    void testForeign() {
        Map.Entry<String, String> entry = null; // Ok
        java.util.Map.Entry<String, String> entry2 = null; // Not Ok - import java.util.Map
    }
}

class TestContainers {
    private java.util.Scanner[] scanners; // Not Ok - import java.util.Scanner
    private List<java.lang.Integer> integers; // Not Ok - import java.lang.Integer
    private java.lang.Double[][] doubles; // Not Ok - import java.lang.Double
}
