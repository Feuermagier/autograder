package de.firemage.autograder.core.check_tests.UseStringFormatted.code;

public class Test {
    public static void main(String[] args) {
        System.out.println(String.format("Hello %s%s", "World", "!")); /*# not ok #*/
        System.out.println(String.format("Hello %d%f", 1, 3.14)); /*# not ok #*/
    }
}
