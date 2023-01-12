package de.firemage.autograder.core.check_tests.UseOperatorAssignment.code;

public class Test {
    public static void main(String[] args) {
        int a = 1;
        int b = 1;
        int c = 1;
        int d = 1;

        a = a + b; // Not Ok
        a = a - b; // Not Ok
        a = a * b; // Not Ok
        a = a / b; // Not Ok
        a = a % b; // Not Ok

        a = b + a; // Not Ok
        a = b - a; // Ok
        a = b * a; // Not Ok
        a = b / a; // Ok
        a = b % a; // Ok

        int[] arr = new int[10];
        arr[0] = arr[0] + 1; // Not Ok
        arr[0] = arr[1] + 1; // Ok

        a = a - b + c; // Ok
        a = b - c + a; // Not Ok
        a = (b + c) * a; // Not Ok
        a = b + c * a; // Ok
        a = b - a + c; // Ok
        a = a - b + c - d; // Ok
    }
}
