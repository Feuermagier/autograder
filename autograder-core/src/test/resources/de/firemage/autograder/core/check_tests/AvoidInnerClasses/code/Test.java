package de.firemage.autograder.core.check_tests.AvoidInnerClasses.code;

public class Test {
    public static void main(String[] args) {
        class InnerClass { // Not Ok
        }
    }

    static class InnerClass { // Not Ok

        static class InnerClass2 { // Not Ok
        }
    }

    private static class Node { // Ok
    }

    public class Node2 { // Ok
    }

    enum InnerEnum { // Not Ok
    }

    interface InnerInterface { // Not Ok
    }

    record InnerRecord() { // Not Ok
    }
}
