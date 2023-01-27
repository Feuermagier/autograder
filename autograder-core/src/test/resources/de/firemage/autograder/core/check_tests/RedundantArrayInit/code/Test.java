package de.firemage.autograder.core.check_tests.RedundantArrayInit.code;

public class Test {
    public static void main(String[] args) {
        args = null; // Ok
        args = new String[3];
        args[0] = null; // Not Ok
        args[1] = null; // Not Ok
        for (int i = 0; i < args.length; i++) {
            args[i] = null; // Not Ok
        }

        args[0] = "foo"; // Ok
        if (args[2] == null) {
            args[2] = "bar"; // Ok
        }

        args[2] = null; // Ok
        args[2] = new String(); // Ok
    }

    private void testBlocks() {
        int[] arr = new int[10];

        if (arr[0] == 0) {
            arr[1] = 0; // Not Ok
        } else if (arr[1] == 0) {
            arr[2] = 0; // Not Ok
        } else {
            arr[3] = 0; // Not Ok
        }

        {
            arr[4] = 0; // Not Ok
        }

        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0; // Not Ok
        }

        for (int i : arr) {
            i = 0; // Ok
        }

        for (int i : arr) {
            arr[i] = 0; // Not Ok
        }

        while (arr[0] != 0) {
            arr[0] = 0; // Not Ok
        }
    }

    private void testArrayTypes() {
        int[] a = new int[10];
        a[0] = 0; // Not Ok
        double[] b = new double[10];
        b[0] = 0.0; // Not Ok
        float[] c = new float[10];
        c[0] = 0.0f; // Not Ok
        long[] d = new long[10];
        d[0] = 0L; // Not Ok
        short[] e = new short[10];
        e[0] = (short) 0; // Not Ok
        byte[] f = new byte[10];
        f[0] = (byte) 0; // Not Ok
        char[] g = new char[10];
        g[0] = (char) 0; // Not Ok
        boolean[] h = new boolean[10];
        h[0] = false; // Not Ok
    }

    private void testDoubleArray() {
        boolean[][] arr = new boolean[10][10];
        arr[0][0] = false; // Not Ok
        arr[0] = null; // Ok
    }

    private void sanityTest() {
        int a = 0;
        a = 1; // Ok
        a = 2; // Ok
        String hello = "hello";
        hello = "world"; // Ok
    }

    private void testConstantsInference() {
        int[] arr = new int[10];
        int a = 0;
        int b = a;
        int c = arr[b];

        arr[a] = 0; // Not Ok
        arr[b] = 0; // Not Ok
        arr[c] = 0; // Not Ok

        arr[a] = a; // Not Ok
        arr[b] = b; // Not Ok
        arr[c] = c; // Not Ok
    }

    private void testSimpleInferenceDoubleArray() {
        String[][] arr = new String[10][10];

        int a = 0;
        int b = a;
        int c = b;

        arr[a][b] = null; // Not Ok
        arr[b][c] = null; // Not Ok
        arr[c][c] = null; // Not Ok

        String value = null;
        String value2 = value;
        String value3 = arr[a][b];

        arr[a][b] = value; // Not Ok
        arr[b][c] = value2; // Not Ok
        arr[c][c] = value3; // Not Ok

        a = 1;
        b = 2;
        c = b;

        arr[a][b] = null; // Not Ok
        arr[b][c] = null; // Not Ok
        arr[c][c] = null; // Not Ok

        arr[a][b] = value; // Not Ok
        arr[b][c] = value2; // Not Ok
        arr[c][c] = value3; // Not Ok
    }

    private void testInferenceNonConstant() {
        int[] arr = new int[10];

        var scanner = new java.util.Scanner(System.in);
        int a = scanner.nextInt(); // can be literally any number
        arr[a] = 0; // Not Ok
        arr[a] = a; // Ok
        int b = scanner.nextInt(); // can be literally any number
        arr[b] = 0; // Ok
    }

    private void testAssignmentWithInit() {
        int[] arr = new int[] { 0, 1, 2, 3 };
        arr[0] = 0; // Not Ok
        arr[1] = 1; // Not Ok
        arr[2] = 2; // Not Ok
        arr[3] = 3; // Not Ok

        arr[0] = 1; // Ok
        arr[1] = 2; // Ok
        arr[2] = 3; // Ok
        arr[3] = 4; // Ok
    }

    private void testNonConstLoopInference() {
        int[] arr = new int[10];

        var scanner = new java.util.Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int c = scanner.nextInt();

        for (int i = 0; i < b; i++) {
            arr[i] = 0; // Not Ok
        }

        for (int i = 0; i < b; i++) {
            arr[i] = 2;
        }

        arr[0] = 2; // Ok
        arr[9] = 2; // Ok

        // this might legitimately overwrite values other than 0
        for (int i = 0; i < c; i++) {
            arr[i] = 0; // Ok
        }
    }
}
