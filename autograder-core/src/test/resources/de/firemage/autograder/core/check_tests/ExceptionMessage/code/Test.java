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

class TestExceptionCatchAndThrow {
    private java.io.File file;

    public String[] readFile() {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(this.file))) {
            return reader.lines().toArray(String[]::new);
        } catch (final java.io.IOException e) {
            throw new RuntimeException(e); // Ok
        }
    }
}
