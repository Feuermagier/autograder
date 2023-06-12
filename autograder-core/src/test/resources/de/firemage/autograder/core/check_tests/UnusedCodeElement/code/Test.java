package de.firemage.autograder.core.check_tests.UnusedCodeElement.code;

public class Test {
    public static void main(String[] args) { // Ok (main method and args are used)
        String firstArg = args[0]; // Not Ok
    }

    private static void foo() {} // Not Ok

    public static void bar() {} // Not Ok
}

// the main method can be called even if the class is only package visible
class SeeminglyUnusedMainMethod {
    public static void main(String[] args) {} // Ok
}

class A {
    int a; // Not Ok
    String[] b; // Not Ok

    void doSomething() {
        int a = 0; // Not Ok
        String[] b = new String[10]; // Not Ok
    }
}
