package de.firemage.autograder.core.check_tests.InterfaceBadPractices.code;

public class Test {
    public static void main(String[] args) {}

    private static interface MyInterface {} /*# not ok #*/
}

interface MyConstantsInterface { /*# not ok #*/
    int MY_CONSTANT = 42;
    String ANOTHER_CONSTANT = "Hello World";
}

interface MyConstantsInterfaceWithMethods {
    int MY_CONSTANT = 42; /*# not ok #*/
    String ANOTHER_CONSTANT = "Hello World"; /*# not ok #*/
    void doSomething();

    default void doSomethingElse() {}
}

interface MyInterfaceWithStaticMethod {
    void doSomething();

    default void doSomethingElse() {}

    static void doesSomething2() {} /*# not ok #*/
}

interface Bar { /*# not ok #*/
}

interface Foo { /*# not ok #*/
    public static final int FOO = 1;
}

class OuterClass {
    interface InnerInterface { /*# ok #*/
        void doSomething();
    }
}
