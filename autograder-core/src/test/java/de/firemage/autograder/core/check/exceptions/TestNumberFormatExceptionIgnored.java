package de.firemage.autograder.core.check.exceptions;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestNumberFormatExceptionIgnored extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.NUMBER_FORMAT_EXCEPTION_IGNORED);

    void assertEqualsIgnored(AbstractProblem problem) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "number-format-exception-ignored"
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testIgnoresException() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    public static void main(String[] args) {
                        int number = Integer.parseInt("123");
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertEqualsIgnored(problems.next());

        problems.assertExhausted();
    }

    @Test
    void testHandlesException() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    public static void main(String[] args) {
                        try {
                            Integer.parseInt("123");
                        } catch (NumberFormatException e) {
                            System.out.println("Error");
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
