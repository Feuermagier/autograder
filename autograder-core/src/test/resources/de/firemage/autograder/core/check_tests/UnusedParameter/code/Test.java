package de.firemage.autograder.core.check_tests.UtilityClassCheck.code;

public class Test {} // Ok

class A {
    A() {} // Ok

    A(int a) {} // Not Ok

    void a() {} // Ok

    void foo(int a) {} // Not Ok
}
