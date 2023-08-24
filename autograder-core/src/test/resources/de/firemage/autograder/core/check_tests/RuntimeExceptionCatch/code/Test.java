package de.firemage.autograder.core.check_tests.RuntimeExceptionCatch.code;

public class Test {
    public static void main(String[] args) {
        try {
            System.out.println("Hi");
        } catch (RuntimeException ex) { /*# not ok #*/
        }

        try {
            Integer.parseInt("foo");
        } catch (NumberFormatException ex) { /*# ok #*/
        } catch (IllegalArgumentException ex) { /*# not ok #*/
        }

        try {
            foo();
        } catch (TextException ex) { /*# ok #*/
        }

        try {
            Integer.parseInt("foo");
        } catch (RuntimeException ex) { /*# ok #*/
            throw new IllegalStateException(ex);
        }
    }

    private static void foo() throws TextException {
        throw new TextException();
    }
}

class TextException extends Exception {}

// See: https://github.com/Feuermagier/autograder/issues/192
class Reproduce {
    public enum Fruit {
        APPLE, PINEAPPLE;
    }

    public static class ParserException extends Exception {
        public ParserException(Throwable cause) {
            super("failed to parse input", cause);
        }
    }

    public static Fruit fromString(String string) throws ParserException {
        try {
            return Fruit.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException exception) { // Ok
            // failed to parse input, exception is not thrown by user
            throw new ParserException(exception);
        }
    }
}
