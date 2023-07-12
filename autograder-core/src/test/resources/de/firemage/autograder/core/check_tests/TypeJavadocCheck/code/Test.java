package de.firemage.autograder.core.check_tests.TypeJavadocCheck.code;

public class Test {
    public static void main(String[] args) {}
}

/**  /*# not ok #*/
 * See issue https://github.com/Feuermagier/autograder/issues/120
 *
 * @author Foo
 * @author Bar
 * @author Baz
 */
class DeduplicateMultipleAuthorTagViolations {}
