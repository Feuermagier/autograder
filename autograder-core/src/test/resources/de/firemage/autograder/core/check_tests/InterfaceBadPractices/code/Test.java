package de.firemage.autograder.core.check_tests.InterfaceBadPractices.code;

public class Test {
    public static void main(String[] args) {}

    private static interface MyInterface {} /*# not ok #*/
}

interface MyConstantsInterfaceWithMethods {
    void doSomething();

    default void doSomethingElse() {}
}

interface MyInterfaceWithStaticMethod {
    void doSomething();

    default void doSomethingElse() {}
}

interface Bar { /*# not ok #*/
}

class OuterClass {
    interface InnerInterface { /*# ok #*/
        void doSomething();
    }
}
