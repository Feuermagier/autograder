package de.firemage.autograder.core.check_tests.RedundantArrayInit.code;

public class Test {
    public static void main(String[] args) {
        args = null; /*@ ok @*/
        args = new String[3];
        args[0] = null; /*@ not ok @*/
        args[1] = null; /*@ not ok @*/
        for (int i = 0; i < args.length; i++) {
            args[i] = null; /*@ not ok @*/
        }

        args[0] = "foo"; /*@ ok @*/
        if (args[2] == null) {
            args[2] = "bar"; /*@ ok @*/
        }

        args[2] = null; /*@ ok @*/
        args[2] = new String(); /*@ ok @*/
    }

    private void testBlocks() {
        int[] arr = new int[10];

        if (arr[0] == 0) {
            arr[1] = 0; /*@ not ok @*/
        } else if (arr[1] == 0) {
            arr[2] = 0; /*@ not ok @*/
        } else {
            arr[3] = 0; /*@ not ok @*/
        }

        {
            arr[4] = 0; /*@ not ok @*/
        }

        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0; /*@ not ok @*/
        }

        for (int i : arr) {
            i = 0; /*@ ok @*/
        }

        for (int i : arr) {
            arr[i] = 0; /*@ false negative @*/
        }

        while (arr[0] != 0) {
            arr[0] = 0; /*@ false negative @*/
        }
    }

    private void testArrayTypes() {
        int[] a = new int[10];
        a[0] = 0; /*@ not ok @*/
        double[] b = new double[10];
        b[0] = 0.0; /*@ not ok @*/
        float[] c = new float[10];
        c[0] = 0.0f; /*@ not ok @*/
        long[] d = new long[10];
        d[0] = 0L; /*@ not ok @*/
        short[] e = new short[10];
        e[0] = (short) 0; /*@ not ok @*/
        byte[] f = new byte[10];
        f[0] = (byte) 0; /*@ not ok @*/
        char[] g = new char[10];
        g[0] = (char) 0; /*@ not ok @*/
        boolean[] h = new boolean[10];
        h[0] = false; /*@ not ok @*/
    }

    private void testDoubleArray() {
        boolean[][] arr = new boolean[10][10];
        arr[0][0] = false; /*@ not ok @*/
        arr[0] = null; /*@ ok @*/
    }

    private void sanityTest() {
        int a = 0;
        a = 1; /*@ ok @*/
        a = 2; /*@ ok @*/
        String hello = "hello";
        hello = "world"; /*@ ok @*/
    }

    private void testConstantsInference() {
        int[] arr = new int[10];
        int a = 0;
        int b = a;
        int c = arr[b];

        arr[a] = 0; /*@ not ok @*/
        arr[b] = 0; /*@ not ok @*/
        arr[c] = 0; /*@ not ok @*/
        arr[a] = a; /*@ not ok @*/
        arr[b] = b; /*@ not ok @*/
        arr[c] = c; /*@ not ok @*/
    }

    private void testSimpleInferenceDoubleArray() {
        String[][] arr = new String[10][10];

        int a = 0;
        int b = a;
        int c = b;

        arr[a][b] = null; /*@ not ok @*/
        arr[b][c] = null; /*@ not ok @*/
        arr[c][c] = null; /*@ not ok @*/
        String value = null;
        String value2 = value;
        String value3 = arr[a][b];

        arr[a][b] = value; /*@ not ok @*/
        arr[b][c] = value2; /*@ not ok @*/
        arr[c][c] = value3; /*@ not ok @*/
        a = 1;
        b = 2;
        c = b;

        arr[a][b] = null; /*@ not ok @*/
        arr[b][c] = null; /*@ not ok @*/
        arr[c][c] = null; /*@ not ok @*/
        arr[a][b] = value; /*@ not ok @*/
        arr[b][c] = value2; /*@ not ok @*/
        arr[c][c] = value3; /*@ not ok @*/
    }

    private void testInferenceNonConstant() {
        int[] arr = new int[10];

        var scanner = new java.util.Scanner(System.in);
        int a = scanner.nextInt(); // can be literally any number
        arr[a] = 0; /*@ not ok @*/
        arr[a] = a; /*@ ok @*/
        int b = scanner.nextInt(); // can be literally any number
        arr[b] = 0; /*@ ok @*/
    }

    private void testAssignmentWithInit() {
        int[] arr = new int[] { 0, 1, 2, 3 };
        arr[0] = 0; /*@ not ok @*/
        arr[1] = 1; /*@ false negative @*/
        arr[2] = 2; /*@ false negative @*/
        arr[3] = 3; /*@ false negative @*/
        arr[0] = 1; /*@ ok @*/
        arr[1] = 2; /*@ ok @*/
        arr[2] = 3; /*@ ok @*/
        arr[3] = 4; /*@ ok @*/
    }

    private void testNonConstLoopInference() {
        int[] arr = new int[10];

        var scanner = new java.util.Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int c = scanner.nextInt();

        for (int i = 0; i < b; i++) {
            arr[i] = 0; /*@ not ok @*/
        }

        for (int i = 0; i < b; i++) {
            arr[i] = 2;
        }

        arr[0] = 2; /*@ ok @*/
        arr[9] = 2; /*@ ok @*/
        // this might legitimately overwrite values other than 0
        for (int i = 0; i < c; i++) {
            arr[i] = 0; /*@ ok @*/
        }
    }
}
