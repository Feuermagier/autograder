package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRedundantNegationCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.REDUNDANT_NEGATION);

    void assertEqualsRedundant(Problem problem, String suggestion) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "common-reimplementation",
                Map.of("suggestion", suggestion)
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        useHeadersInDisplayName = true,
        value = {
            " Expression          ; Arguments             ; Expected     ",
            " !(!b)               ; boolean b             ; b            ",
            " !(b)                ; boolean b             ;              ",
            " !(a == b)           ; int a, int b          ; a != b       ",
            " !(a != b)           ; int a, int b          ; a == b       ",
            " !(a && b)           ; boolean a, boolean b  ; (!a) || (!b) ",
            " !(a || b)           ; boolean a, boolean b  ; (!a) && (!b) ",
            " !(a >= b)           ; int a, int b          ; a < b        ",
            " !(a > b)            ; int a, int b          ; a <= b       ",
            " !(a <= b)           ; int a, int b          ; a > b        ",
            " !(a < b)            ; int a, int b          ; a >= b       ",
            " !(a ^ b)            ; boolean a, boolean b  ; a == b       ",
        }
    )
    void testRedundantNegation(String expression, String arguments, String expected) throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            "public class Test { public void foo(%s) { System.out.println(%s); } }".formatted(
                arguments,
                expression
            )
        ), PROBLEM_TYPES);

        if (expected != null) {
            assertEqualsRedundant(problems.next(), expected);
        }

        problems.assertExhausted();
    }

    @Test
    void testNegatedInvocation() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.List;

                public class Test {
                    public void foo(List<Object> list) {
                        System.out.println(!((boolean) list.get(0)));
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
