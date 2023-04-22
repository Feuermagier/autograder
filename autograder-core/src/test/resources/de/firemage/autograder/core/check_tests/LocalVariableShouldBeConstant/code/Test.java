package de.firemage.autograder.core.check_tests.LocalVariableShouldBeConstant.code;

public class Test {
    public static void main(String[] args) {
        final int numberOfThingsToDo = 4; // Not Ok
        doSomething(numberOfThingsToDo);
    }

    private static void doSomething(int value) {}
}
