package de.firemage.autograder.core.check.oop;

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

class TestStaticFieldShouldBeInstanceCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.STATIC_FIELD_SHOULD_BE_INSTANCE);

    void assertShouldBeInstance(Problem problem, String name) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "static-field-should-be-instance",
                Map.of("name", name)
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testShouldBeInstance() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    private static int a = 0; //# not ok
                    private static int b = 1; //# ok, effectively final

                    public static void main(String[] args) {
                        a = 1;

                        System.out.println(a);
                        System.out.println(b);
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertShouldBeInstance(problems.next(), "a");

        problems.assertExhausted();
    }
}
