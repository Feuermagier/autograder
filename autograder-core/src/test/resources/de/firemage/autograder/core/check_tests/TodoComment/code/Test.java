package de.firemage.autograder.core.check_tests.TodoComment.code;

public class Test {
    public static void main(String[] args) {
        // TODO: do something /*# not ok #*/
        // TODO do something /*# not ok #*/
        //TODO do something /*# not ok #*/
        // T O D O do something /*# ok #*/
        /**
         * TODO do something //# ok
         */
    }
}
