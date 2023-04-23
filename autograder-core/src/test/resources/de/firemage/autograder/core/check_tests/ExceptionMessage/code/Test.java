package de.firemage.autograder.core.check_tests.ExceptionMessage.code;

public class Test {
    void a() {
        throw new IllegalArgumentException("foo"); // ok
    }

    void b() {
        throw new IllegalArgumentException(); // Not ok
    }

    void c() {
        throw new IllegalArgumentException(""); // Not ok
    }

    void d() {
        throw new IllegalArgumentException(" "); // Not ok
    }

    void e() throws Bar {
        throw new Bar(); // Ok
    }

    void f() {
        throw new IllegalArgumentException("", new Exception()); // Not ok
    }
}

class Bar extends Exception {
    public Bar() {
        super("Bar");
    }
}

class TestInSwitchDefault {
    void foo(String string) {
        switch (string) {
            case "a" -> {}
            case "b" -> {}
            default -> throw new IllegalStateException(); // Ok
        }
    }
}
