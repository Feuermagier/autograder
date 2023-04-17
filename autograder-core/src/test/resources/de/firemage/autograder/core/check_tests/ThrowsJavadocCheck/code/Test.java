package de.firemage.autograder.core.check_tests.ThrowsJavadocCheck.code;

import java.util.*;

public class Test {
    /**
     * @throws IllegalArgumentException if the argument is null
     */
    public Test() {
        throw new IllegalArgumentException("Error"); // Ok
    }

    /**
     * @throws IllegalStateException
     */
    public Test(String a) {
        throw new IllegalArgumentException("Error"); // Not Ok
    }

    private Test(int a) {
        throw new IllegalArgumentException("Error"); // Ok (constructor is private)
    }

    /**
     * @throws RuntimeException
     */
    public static void main(String[] args) {
        throw new RuntimeException(); // Ok
    }

    /**
     * @throws IllegalStateException something went wrong
     * @throws NullPointerException if the argument is null
     */
    public static void a() {
        throw new IllegalArgumentException("Error"); // Not Ok
    }

    /**
     * @throws IllegalStateException something went wrong
     * @throws NullPointerException if the argument is null
     */
    private static void b() {
        throw new IllegalArgumentException("Error"); // Ok (method is private)
    }
}
