package de.firemage.autograder.core.check_tests.AvoidInnerClasses.code;

public class Test {
    public static void main(String[] args) {
        class InnerClass { /*@ not ok @*/
        }
    }

    static class InnerClass { /*@ not ok @*/
        static class InnerClass2 { /*@ not ok @*/
        }
    }

    private static class Node { /*@ ok @*/
    }

    public class Node2 { /*@ ok @*/
    }

    enum InnerEnum { /*@ not ok @*/
    }

    interface InnerInterface { /*@ not ok @*/
    }

    record InnerRecord() { /*@ not ok @*/
    }
}
