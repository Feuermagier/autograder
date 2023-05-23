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

class TestAttributeReassignment {
    private boolean isOk;

    public void foo() {
        this.isOk = isOk; // Not Ok
        this.isOk = this.isOk; // Not Ok
        isOk = this.isOk; // Not Ok
        isOk = isOk; // Not Ok
    }

    public void foo2(boolean isOk) {
        this.isOk = isOk; // Ok
    }
}

class Parent {
    protected int a = 0;
    protected int b = 1;
}

class TestParentReassignment extends Parent {
    private int b = 2;

    public void foo() {
        super.a = a; // Not Ok
        super.a = super.a; // Not Ok
        a = super.a; // Not Ok
        a = a; // Not Ok
    }

    public void foo2() {
        super.b = b; // Ok
        super.b = super.b; // Not Ok
        b = super.b; // Ok
        b = b; // Not Ok
    }

    public void foo3(int b) {
        super.b = b; // Ok
        b = super.b; // Ok
        b = b; // Not Ok
    }
}
