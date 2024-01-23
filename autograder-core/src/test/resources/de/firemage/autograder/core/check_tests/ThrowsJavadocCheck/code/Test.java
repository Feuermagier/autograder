package de.firemage.autograder.core.check_tests.ThrowsJavadocCheck.code;

import java.util.*;

public class Test {
    /**
     * @throws IllegalArgumentException if the argument is null
     */
    public Test() {
        throw new IllegalArgumentException("Error"); /*# ok #*/
    }

    /**
     * @throws IllegalStateException
     */
    public Test(String a) {
        throw new IllegalArgumentException("Error"); /*# not ok #*/
    }

    private Test(int a) {
        throw new IllegalArgumentException("Error"); /*# ok; constructor is private #*/
    }

    /**
     * @throws RuntimeException
     */
    public static void main(String[] args) {
        throw new RuntimeException(); /*# ok #*/
    }

    /**
     * @throws IllegalStateException something went wrong
     * @throws NullPointerException if the argument is null
     */
    public static void a() {
        throw new IllegalArgumentException("Error"); /*# not ok #*/
    }

    /**
     * @throws IllegalStateException something went wrong
     * @throws NullPointerException if the argument is null
     */
    private static void b() {
        throw new IllegalArgumentException("Error"); /*# ok; method is private #*/
    }


    /**
     * @exception IllegalStateException something went wrong
     * @exception IllegalArgumentException if the argument is null
     */
    public static void c() {
        throw new IllegalArgumentException("Error"); /*# ok #*/
    }
}
