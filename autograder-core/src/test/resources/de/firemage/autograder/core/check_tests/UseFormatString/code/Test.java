package de.firemage.autograder.core.check_tests.UseFormatString.code;

public class Test {
    public static void main(String[] args) {
        String value;
        value = "a" + "b" + "c"; /*# not ok #*/
        value = "Hello " + 1 + " " + 3.14f; /*# not ok #*/
    }
}

class TestVariableResolver {
    static final String a = "hello";
    static final float b = 3.14f;

    private static void function() {
        final String c = "cee";
        String value;

        value = a + " b " + c; /*# not ok #*/
        value = a + " " + 1 + " " + b; /*# not ok #*/
    }
}

class TestStringBuilder {
    static final String a = "hello";
    static final float b = 3.14f;

    private static void function() {
        final String c = "cee";
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("a").append("b").append("c"); /*# not ok #*/
        stringBuilder.append("Hello ").append(1).append(" ").append(3.14f); /*# not ok #*/
        stringBuilder.append("singleValue"); /*# ok #*/
        stringBuilder.append(a).append(" b ").append(c); /*# not ok #*/
        stringBuilder.append(a).append(" ").append(1).append(" ").append(b); /*# not ok #*/
    }
}
