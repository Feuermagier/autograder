package de.firemage.autograder.core.check_tests.JavadocReturnNull.code;

public class Test {
    public int a; // Ok, because public
    private int b; // Not ok, because always overwritten
    private Object c; // Not ok, because always overwritten
    private Object d = new Object(); // Ok, because not always overwritten before being read
    
    public static void main(String[] args) {
        Test test = new Test();
        test.returnAlwaysNullMentioned();
        test.returnAlwaysNullNotMentioned();
        test.returnNeverNull();
        test.returnSometimesNullMentioned(0);
        test.returnSometimesNullMentioned(1);
        test.returnSometimesNullNotMentioned(0);
        test.returnSometimesNullNotMentioned(1);
        test.returnAlwaysNullMissingJavadoc();
        test.returnAlwaysNullMissingReturnTag();
        test.returnInt();
        test.privateReturnAlwaysNullWithJavadoc();
    }

    /**
     * ...
     * @return the value or null
     */
    public Object returnAlwaysNullMentioned() {
        if (true) {
            return null;
        } else {
            return false;
        }
    }

    /**
     * ...
     * @return the value
     */
    public Object returnAlwaysNullNotMentioned() {
        if (true) {
            return null;
        } else {
            return false;
        }
    }

    /**
     * ...
     * @return the value
     */
    public Object returnNeverNull() {
        if (false) {
            return null;
        } else {
            return false;
        }
    }

    /**
     * ...
     * @return the value or null
     */
    public Object returnSometimesNullMentioned(int x) {
        if (x > 0) {
            return new Object();
        } else {
            return null;
        }
    }

    /**
     * ...
     * @return the value
     */
    public Object returnSometimesNullNotMentioned(int x) {
        if (x > 0) {
            return new Object();
        } else {
            return null;
        }
    }
    
    public Object returnAlwaysNullMissingJavadoc() {
        return null;
    }

    /**
     * ...
     */
    public Object returnAlwaysNullMissingReturnTag() {
        return null;
    }

    /**
     * ...
     */
    public int returnInt() {
        return 0;
    }

    /**
     * ...
     * @return the value
     */
    private Object privateReturnAlwaysNullWithJavadoc() {
        return null;
    }
}
