package de.firemage.autograder.core.check_tests.IOUISeparation.code;

import java.util.Scanner;

public class Test {
    public Test() {
        Scanner scanner = new Scanner(System.in); // Ok
        String input = scanner.nextLine(); // Ok

        System.out.println("Test!"); // Ok
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Ok
        String input = scanner.nextLine(); // Ok

        System.out.println("Hello World!"); // Ok
        System.out.print("Hello World!"); // Ok
    }

    public static void eprint() {
        System.err.println(); // Ok
        Scanner scanner = new Scanner(System.in); // Ok
        int input = scanner.nextInt(); // Ok
    }
}

class SecondClass {
    public SecondClass() {
        System.out.println("Second Class!"); // Not Ok
        Scanner scanner = new Scanner(System.in); // Ok
        String string = scanner.nextLine(); // Not Ok
    }

    public void print(String string) {
        System.err.print(string); // error only reported once per class
        Scanner scanner = new Scanner(System.in); // Ok
        String s2 = scanner.nextLine(); // error only reported once per class
    }


    static class ThirdClass {
        public static void printf(String string) {
            System.out.printf(string); // Not Ok
            Scanner scanner = new Scanner(System.in); // Ok
            String s2 = scanner.nextLine(); // Not Ok
        }
    }
}

record MyRecord(String string) {
    public MyRecord {
        System.out.println("Record!"); // Not Ok
        Scanner scanner = new Scanner(System.in); // Ok
        String s = scanner.nextLine(); // Not Ok
    }

    public void print(String string) {
        System.err.print(string); // error only reported once per class
        Scanner scanner = new Scanner(System.in); // Ok
        String s2 = scanner.nextLine(); // error only reported once per class
    }
}

enum MyEnum {
    A, B, C;

    public void print(String string) {
        System.err.print(string); // Not Ok
    }

    public static void printf(String string) {
        System.out.printf(string); // error only reported once per class
    }
}
