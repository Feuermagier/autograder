package de.firemage.autograder.core.check_tests.ImportTypes.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        java.lang.String string = "Hello World"; /*# not ok; import java.lang.String #*/
    }
}

class TestInnerClass {
    static class A { static class B {} }

    void testKnown() {
        A a = new A(); /*# ok #*/
        A.B b = new A.B(); /*# ok #*/
    }

    void testForeign() {
        Map.Entry<String, String> entry = null; /*# ok #*/
        java.util.Map.Entry<String, String> entry2 = null; /*# not ok #*/
    }
}

class TestContainers {
    private java.util.Scanner[] scanners; /*# not ok; import java.util.Scanner #*/
    private List<java.lang.Integer> integers; /*# not ok; import java.lang.Integer #*/
    private java.lang.Double[][] doubles; /*# not ok; import java.lang.Double #*/

    private void foo(String... varargs) {} /*# ok #*/
    private void bar(java.lang.String... varargs) {}  /*# not ok; import java.lang.String #*/
}

class VisualRepresentation {
    VisualRepresentation(String... lines) { /*# ok #*/
        var stream = Arrays.stream(lines); /*# ok #*/
    }

    VisualRepresentation(java.lang.Integer... values) { /*# not ok #*/
    }

    private void call(String... varargs) { /*# ok #*/
        takesArray(new String[] {"Hello", "World"}); /*# ok #*/
        String[] array = varargs; /*# ok #*/
        takesArray(array); /*# ok #*/
        takesArray(varargs); /*# ok; there is an implicit cast to Object[] #*/
    }

    private <T> void takesArray(T[] array) {} /*# ok #*/
}

class FalsePositiveToArray {
    public static  <T> String[] takesList(List<T> list) {
        return list.stream().map(Object::toString).toArray(String[]::new); /*# ok #*/
    }
}

class FalsePositiveArrayClassLiteral {
    public static String[] makeCopy(String[] array) {
        System.out.println(Integer.class); /*# ok #*/
        return Arrays.copyOf(array, array.length, String[].class); /*# ok #*/
    }
}
