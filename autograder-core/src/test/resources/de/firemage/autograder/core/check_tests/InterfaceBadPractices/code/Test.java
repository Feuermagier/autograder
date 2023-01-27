package de.firemage.autograder.core.check_tests.InterfaceBadPractices.code;

public class Test {
    public static void main(String[] args) {}

    private static interface MyInterface {} // Not Ok
}

interface MyConstantsInterface {
    int MY_CONSTANT = 42; // Not Ok
    String ANOTHER_CONSTANT = "Hello World"; // Not Ok
}

interface MyConstantsInterfaceWithMethods {
    int MY_CONSTANT = 42; // Not Ok
    String ANOTHER_CONSTANT = "Hello World"; // Not Ok

    void doSomething();

    default void doSomethingElse() {}
}

interface MyInterfaceWithStaticMethod {
    void doSomething();

    default void doSomethingElse() {}

    static void doesSomething2() {} // Not Ok
}

interface Bar {
}

interface Foo {
    public static final int FOO = 1; // Not ok
}
