package de.firemage.autograder.core.check_tests.RedundantCatch.code;

public class Test {
    public static void main(String[] args) {}

    private void execute() throws IllegalArgumentException {
        throw new IllegalArgumentException("This is a test exception");
    }

    private void doExecute() {
        try {
            execute();
        } catch (IllegalArgumentException exception) {
            // caught the exception
            throw exception; /*@ not ok @*/
        }
    }
}
