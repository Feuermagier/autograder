package de.firemage.autograder.core.check_tests.MultiThreading.code;

import java.util.regex.Pattern;

public class Test {
    public synchronized /*# not ok #*/ void test() {
        System.out.println("Hello World");
    }

    public void test2() {
        synchronized (this /*# not ok #*/) {
            System.out.println("Hello World");
        }
    }
}
