package de.firemage.autograder.core.check_tests.AvoidShadowing.code;

public class Test {
    int x;
    static int y;
    private int z;

    public void foo() {
        int x = 4; /*@ not ok @*/
        int y = 5; /*@ not ok @*/
        final int z = 5; /*@ not ok @*/
    }
}

class A {
    protected int a;
    int x;
    static int y;
    private int z;
}

class B extends A {
    private final int b;
    private final int c;

    public B(int b, int c) { /*@ ok @*/
        this.b = b;
        this.c = c;
    }

    private void foo2(int b) {} /*@ not ok @*/
    private void foo() {
        int a = 3; /*@ not ok @*/
        int x = 4; /*@ not ok @*/
        int y = 5; /*@ not ok @*/
        final int z = 5; /*@ ok @*/
    }
}

class C extends A {
    protected int a; /*@ not ok @*/
    int x; /*@ not ok @*/
    static int y; /*@ not ok @*/
    private int z; /*@ ok @*/
}

class SomeException extends IllegalArgumentException {
    @java.io.Serial
    private static final long serialVersionUID = -4491591333105161142L; /*@ ok @*/
    public SomeException(String message) {
        super(message);
    }
}

class Parent {
    void parent(int x) {}
}

class Child extends Parent {
    private int x;

    void parent(int x) {} /*@ ok @*/
}

class ShadowInStaticContext {
    private String string;

    public static void doSomething(String string) {} /*@ ok @*/
}
