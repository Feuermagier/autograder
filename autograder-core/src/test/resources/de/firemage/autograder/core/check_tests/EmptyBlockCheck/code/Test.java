package de.firemage.autograder.core.check_tests.EmptyBlockCheck.code;

public class Test {
    public static void main(String[] args) {
        if (true) {} /*# not ok #*/
        while (args[0].isEmpty()) {} /*# not ok #*/
        try {
            throw new IllegalArgumentException();
        } catch (Exception e) /*# not ok #*/ {
        } finally {} /*# not ok #*/
    }

    interface A {
        void call();
    }

    class B implements A {
        @Override
        public void call() {} /*# not ok #*/
    }

    class C implements A {
        @Override
        public void call() {
            // empty for some good reason
        } /*# ok #*/
    }

    void foo(int a) {
        switch (a) {
            case 1: /*# ok #*/
                break;
            default:
                break;
        }

        switch (a) {
            case 1: {
                break;
            }
            default: {
                break;
            }
        }

        // empty block
        {} /*# not ok #*/
        if (a == 5) {
            System.out.println("a is 5");
        } else if (a == 3) /*# not ok #*/ {
        } else /*# not ok #*/ {
        }

        switch (a) { /*# not ok #*/
        }

        try {
            System.out.println("Hello World!");
        } catch (Exception e) { /*# ok #*/
            // effectively empty
        }

        try {
            System.out.println("Hello World!");
        } catch (Exception e) {} /*# not ok #*/
    }
}

final class UtilityClass {
    private UtilityClass() {} /*# ok #*/
    private static void foo() {
        System.out.println("foo");
    }
}

class NormalClass {
    NormalClass() {
    } /*# ok; covered by RedundantConstructorCheck #*/
}
