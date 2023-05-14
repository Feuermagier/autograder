package de.firemage.autograder.core.check_tests.UnusedParameter.code;

public class Test {} // Ok

class A {
    A() {} // Ok

    A(int a) {} // Not Ok

    void a() {} // Ok

    void foo(int a) {} // Not Ok
}

class MainClass {
    public static void main(String[] args) {} // Ok (main method and args should be ignored)

    @Override
    public boolean equals(Object o) { // Ok (overridden method)
        return super.equals(o);
    }
}
