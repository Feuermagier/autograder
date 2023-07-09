package de.firemage.autograder.core.check_tests.IOUISeparation.code;

import java.util.Scanner;

public class Test {
    public Test() {
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        String input = scanner.nextLine(); /*@ ok @*/
        System.out.println("Test!"); /*@ ok @*/
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        String input = scanner.nextLine(); /*@ ok @*/
        System.out.println("Hello World!"); /*@ ok @*/
        System.out.print("Hello World!"); /*@ ok @*/
    }

    public static void eprint() {
        System.err.println(); /*@ ok @*/
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        int input = scanner.nextInt(); /*@ ok @*/
    }
}

class SecondClass {
    public SecondClass() {
        System.out.println("Second Class!"); /*@ not ok @*/
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        String string = scanner.nextLine(); /*@ not ok @*/
    }

    public void print(String string) {
        System.err.print(string); // error only reported once per class
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        String s2 = scanner.nextLine(); // error only reported once per class
    }


    static class ThirdClass {
        public static void printf(String string) {
            System.out.printf(string); /*@ not ok @*/
            Scanner scanner = new Scanner(System.in); /*@ ok @*/
            String s2 = scanner.nextLine(); /*@ not ok @*/
        }
    }
}

record MyRecord(String string) {
    public MyRecord {
        System.out.println("Record!"); /*@ not ok @*/
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        String s = scanner.nextLine(); /*@ not ok @*/
    }

    public void print(String string) {
        System.err.print(string); // error only reported once per class
        Scanner scanner = new Scanner(System.in); /*@ ok @*/
        String s2 = scanner.nextLine(); // error only reported once per class
    }
}

enum MyEnum {
    A, B, C;

    public void print(String string) {
        System.err.print(string); /*@ not ok @*/
    }

    public static void printf(String string) {
        System.out.printf(string); // error only reported once per class
    }
}
