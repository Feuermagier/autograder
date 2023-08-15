package de.firemage.autograder.core.check_tests.UseOperatorAssignment.code;

public class Test {
    public static void main(String[] args) {
        int a = 1;
        int b = 1;
        int c = 1;
        int d = 1;

        a = a + b; /*# not ok #*/
        a = a - b; /*# not ok #*/
        a = a * b; /*# not ok #*/
        a = a / b; /*# not ok #*/
        a = a % b; /*# not ok #*/

        a = b + a; /*# not ok #*/
        a = b - a; /*# ok #*/
        a = b * a; /*# not ok #*/
        a = b / a; /*# ok #*/
        a = b % a; /*# ok #*/

        int[] arr = new int[10];
        arr[0] = arr[0] + 1; /*# not ok #*/
        arr[0] = arr[1] + 1; /*# ok #*/

        a = a - b + c; /*# ok #*/
        a = b - c + a; /*# not ok #*/
        a = (b + c) * a; /*# not ok #*/
        a = b + c * a; /*# ok #*/
        a = b - a + c; /*# ok #*/
        a = a - b + c - d; /*# ok #*/

        String s = "a";
        s = s + " "; /*# not ok #*/
        s = " " + s; /*# ok #*/
    }
}
