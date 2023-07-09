package de.firemage.autograder.core.check_tests.ScannerClosedCheck.code;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.close(); /*@ ok @*/
    }
}
