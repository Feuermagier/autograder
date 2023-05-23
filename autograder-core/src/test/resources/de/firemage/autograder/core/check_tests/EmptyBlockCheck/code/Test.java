package de.firemage.autograder.core.check_tests.EmptyBlockCheck.code;

public class Test {
    public static void main(String[] args) {
        if (true) {} // Not Ok

        while (args[0].isEmpty()) {} // Not Ok

        try {
            throw new IllegalArgumentException();
        } catch (Exception e) { // Not Ok
        } finally {} // Not Ok
    }

    interface A {
        void call();
    }

    class B implements A {
        @Override
        public void call() {} // Not Ok
    }

    class C implements A {
        @Override
        public void call() {
            // empty for some good reason
        } // Ok
    }

    void foo(int a) {
        switch (a) {
            case 1: // Ok
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
        {} // Not Ok

        if (a == 5) {
            System.out.println("a is 5");
        } else if (a == 3) {
        } else {
        } // Not Ok

        switch (a) { // Not Ok
        }

        try {
            System.out.println("Hello World!");
        } catch (Exception e) { // Not Ok
            // effectively empty
        }

        try {
            System.out.println("Hello World!");
        } catch (Exception e) {} // Not Ok
    }
}
