package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRepeatedMathOperationCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.REPEATED_MATH_OPERATION);

    void assertEqualsRepeat(String suggestion, Problem problem) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "common-reimplementation",
                Map.of("suggestion", suggestion)
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testRepeat() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public static void main(String[] args) {
                        int a = 4;
                        int b = 3;
                        
                        int c = a + a + a + a;
                        int d = b * b * b * b * b;

                        int[] array = new int[10];
                        int[] array2 = new int[10];

                        int e = array.length * array.length * array.length * array.length * array.length;
                        int f = array2.length * array.length * array2.length;
                    }
                }
                """
        ), PROBLEM_TYPES);


        assertEqualsRepeat("a * 4", problems.next());
        assertEqualsRepeat("Math.pow(b, 5)", problems.next());
        assertEqualsRepeat("Math.pow(array.length, 5)", problems.next());
        problems.assertExhausted();
    }

    @Test
    void testFalsePositiveFieldAccess() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public static void main(String[] args) {
                        int[] a = new int[10];
                        
                        int b = a.length + args.length;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
