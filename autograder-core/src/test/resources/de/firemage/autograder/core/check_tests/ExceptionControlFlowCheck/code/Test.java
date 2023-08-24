package de.firemage.autograder.core.check_tests.ExceptionControlFlowCheck.code;

public class Test {
    static class TopE extends RuntimeException { }
    static class SubE extends TopE { }

    public static void main(String[] args) throws java.io.IOException {
        try { /*# not ok; this is essentially a GOTO to the IllegalStateException catch block #*/
            try {
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } catch (IllegalStateException e) {
            // do some more stuff
        }

        try {} catch (Exception e) {} /*# ok #*/
        String foo = null;
        try { /*# ok #*/
            java.nio.file.Files.size(java.nio.file.Path.of("foo"));
        } catch (java.io.IOException e) {
            if (foo != null)
                throw new java.io.IOException(foo.toString());
            else
                throw e;
        }

        try { /*# ok #*/
            throw new TopE();
        } catch (SubE e) {
        }

        try { /*# not ok #*/
            throw new SubE();
        } catch (TopE e) {
        }
    }

    void extra(String foo) {
        switch(foo) { /*# ok #*/
            default:
                throw new IllegalArgumentException();
        }
    }

    void forbiddenToCatch(boolean a) {
        try {
            String value = null;
            if (a) {
                System.out.println(value.length());
            } else {
                assert value != null;
            }
        } catch (NullPointerException /*# not ok #*/ | AssertionError /*# not ok #*/ e) {
            // ...
        }
    }
}
