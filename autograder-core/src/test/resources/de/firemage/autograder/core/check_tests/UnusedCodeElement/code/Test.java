package de.firemage.autograder.core.check_tests.UnusedCodeElement.code;

public class Test {
    public static void main(String[] args) { //?# ok (main method and args are used)
        String firstArg = args[0]; /*@ not ok @*/
    }

    private static void foo() {} /*@ not ok @*/
    public static void bar() {} /*@ not ok @*/
}

// the main method can be called even if the class is only package visible
class SeeminglyUnusedMainMethod {
    public static void main(String[] args) {} /*@ ok @*/
}

class A {
    int a; /*@ not ok @*/
    String[] b; /*@ not ok @*/
    void doSomething() { /*@ not ok @*/
        int a = 0; /*@ not ok @*/
        String[] b = new String[10]; /*@ not ok @*/
    }

    @Override
    public boolean equals(Object o) { //?# ok (overridden method)
        return super.equals(o);
    } /*@ ok @*/
    void foo() { /*@ not ok @*/
        foo();
    }
}
