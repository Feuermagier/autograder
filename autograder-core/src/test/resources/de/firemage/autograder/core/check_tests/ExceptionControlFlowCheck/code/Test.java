package de.firemage.autograder.core.check_tests.ExceptionControlFlowCheck.code;

public class Test {
    static class TopE extends RuntimeException { }
    static class SubE extends TopE { }

    public static void main(String[] args) throws java.io.IOException {
        try {
            try {
            } catch (Exception e) {
                throw new IllegalStateException(e);
                // this is essentially a GOTO to the IllegalStateException catch block
            }
        } catch (IllegalStateException e) { // Not Ok
            // do some more stuff
        }

        try {} catch (Exception e) {} // Ok

        String foo = null;
        try {
            java.nio.file.Files.size(java.nio.file.Path.of("foo"));
        } catch (java.io.IOException e) { // Ok
            if (foo != null)
                throw new java.io.IOException(foo.toString());
            else
                throw e;
        }

        try {
            throw new TopE();
        } catch (SubE e) { // Ok

        }

        try {
            throw new SubE();
        } catch (TopE e) { // Not Ok

        }
    }

    void extra(String foo) {
        switch(foo) { // Ok
            default:
                throw new IllegalArgumentException();
        }
    }
}
