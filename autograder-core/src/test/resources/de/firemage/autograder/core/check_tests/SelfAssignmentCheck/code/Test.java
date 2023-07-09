package de.firemage.autograder.core.check_tests.SelfAssignmentCheck.code;

public class Test {
    public static void main(String[] args) {
        String a = "a";
        String b = "b";
        String c = "c";

        a = a; /*@ not ok @*/
        b = a; /*@ ok @*/
        c = b; /*@ ok @*/
    }
}

class TestCopyConstructor {
    private final String a;
    private final String b;

    public TestCopyConstructor(String a, String b) {
        this.a = a; /*@ ok @*/
        this.b = b; /*@ ok @*/
    }

    public TestCopyConstructor(TestCopyConstructor other) {
        this.a = other.a; /*@ ok @*/
        this.b = other.b; /*@ ok @*/
    }
}

class TestAttributeReassignment {
    private boolean isOk;

    public void foo() {
        this.isOk = isOk; /*@ not ok @*/
        this.isOk = this.isOk; /*@ not ok @*/
        isOk = this.isOk; /*@ not ok @*/
        isOk = isOk; /*@ not ok @*/
    }

    public void foo2(boolean isOk) {
        this.isOk = isOk; /*@ ok @*/
    }
}

class Parent {
    protected int a = 0;
    protected int b = 1;
}

class TestParentReassignment extends Parent {
    private int b = 2;

    public void foo() {
        super.a = a; /*@ not ok @*/
        super.a = super.a; /*@ not ok @*/
        a = super.a; /*@ not ok @*/
        a = a; /*@ not ok @*/
    }

    public void foo2() {
        super.b = b; /*@ ok @*/
        super.b = super.b; /*@ not ok @*/
        b = super.b; /*@ ok @*/
        b = b; /*@ not ok @*/
    }

    public void foo3(int b) {
        super.b = b; /*@ ok @*/
        b = super.b; /*@ ok @*/
        b = b; /*@ not ok @*/
    }
}
