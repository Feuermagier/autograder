package de.firemage.autograder.core.check_tests.UseFormatString.code;

public class Test {
    public static void main(String[] args) {
        String value;
        value = "a" + "b" + "c"; // Not Ok
        value = "Hello " + 1 + " " + 3.14f; // Not Ok
    }
}

class TestVariableResolver {
    static final String a = "hello";
    static final float b = 3.14f;

    private static void function() {
        final String c = "cee";
        String value;

        value = a + " b " + c; // Not Ok
        value = a + " " + 1 + " " + b; // Not Ok
    }
}

class TestStringBuilder {
    static final String a = "hello";
    static final float b = 3.14f;

    private static void function() {
        final String c = "cee";
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("a").append("b").append("c"); // Not Ok
        stringBuilder.append("Hello ").append(1).append(" ").append(3.14f); // Not Ok
        stringBuilder.append("singleValue"); // Ok
        stringBuilder.append(a).append(" b ").append(c); // Not Ok
        stringBuilder.append(a).append(" ").append(1).append(" ").append(b); // Not Ok
    }
}
