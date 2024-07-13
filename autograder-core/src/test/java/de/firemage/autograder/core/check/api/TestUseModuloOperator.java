package de.firemage.autograder.core.check.api;

import de.firemage.autograder.api.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.AbstractProblem;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.api.JavaVersion;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUseModuloOperator extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.USE_MODULO_OPERATOR);

    private void assertReimplementation(AbstractProblem problem, String suggestion) {
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of("suggestion", suggestion)
                )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }


    @Test
    void testModulo() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    private static final int EMPTY = 0;

                    public static int adjust(int value, int limit) {
                        int result = value;

                        if (result > limit) {
                            result = 0;
                        }

                        if (limit <= result) {
                            result = 0;
                        }
                        
                        if (result == limit) {
                            result = 0;
                        }

                        return result;
                    }
                }
                """
        ), PROBLEM_TYPES);

        List<String> expectedSuggestions = List.of(
            "result %= limit + 1",
            "result %= limit",
            "result %= limit"
        );

        for (String expectedSuggestion : expectedSuggestions) {
            AbstractProblem problem = problems.next();

            assertReimplementation(problem, expectedSuggestion);
        }
        problems.assertExhausted();
    }

    @Test
    void testBoxedEqualsNull() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    public static void test(Integer i) {
                        if (i == null) {
                            i = 0;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testBoxedInteger() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    public static void test(Integer i) {
                        Integer j = 7;
                        if (i == j) {
                            i = 0;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "i %= j");

        problems.assertExhausted();
    }
}
