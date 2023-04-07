package de.firemage.autograder.core.check_tests.SelfAssignmentCheck.code;

public class Test {
    public static void main(String[] args) {
        String a = "a";
        String b = "b";
        String c = "c";

        a = a; // Not ok
        b = a; // Ok
        c = b; // Ok
    }
}

class TestCopyConstructor {
    private final String a;
    private final String b;

    public TestCopyConstructor(String a, String b) {
        this.a = a; // Ok
        this.b = b; // Ok
    }

    public TestCopyConstructor(TestCopyConstructor other) {
        this.a = other.a; // Ok
        this.b = other.b; // Ok
    }
}
