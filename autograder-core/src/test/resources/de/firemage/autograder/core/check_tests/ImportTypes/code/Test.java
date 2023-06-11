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

    private void foo(String... varargs) {} // Ok

    private void bar(java.lang.String... varargs) {} // Not Ok - import java.lang.String
}

class VisualRepresentation {
    VisualRepresentation(String... lines) { // Ok
        var stream = Arrays.stream(lines); // Ok
    }

    VisualRepresentation(java.lang.Integer... values) { // Not Ok
    }

    private void call(String... varargs) { // Ok
        takesArray(new String[] {"Hello", "World"}); // Ok
        String[] array = varargs; // Ok
        takesArray(array); // Ok
        takesArray(varargs); // Ok (there is an implicit cast to Object[])
    }

    private <T> void takesArray(T[] array) {} // Ok
}

class FalsePositiveToArray {
    public static  <T> String[] takesList(List<T> list) {
        return list.stream().map(Object::toString).toArray(String[]::new); // Ok
    }
}
