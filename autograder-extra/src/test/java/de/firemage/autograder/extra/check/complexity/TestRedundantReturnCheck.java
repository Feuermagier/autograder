package de.firemage.autograder.extra.check.complexity;

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

class TestRedundantReturnCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(
        ProblemType.REDUNDANT_VOID_RETURN
    );

    void assertRedundantReturn(AbstractProblem problem) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "redundant-return-exp"
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testRedundantReturnEndVoidMethod() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                Map.entry(
                    "Example",
                    """
                        public class Example {
                            void foo() {
                                return;
                            }
                        }
                        """
                )
            )
        ), PROBLEM_TYPES);

        assertRedundantReturn(problems.next());

        problems.assertExhausted();
    }

    @Test
    void testNotRedundantReturnControlFlow() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                Map.entry(
                    "Example",
                    """
                        public class Example {
                            void foo(int a) {
                                if (a > 0) {
                                    return;
                                }
                                
                                System.out.println("a is not positive");
                            }
                        }
                        """
                )
            )
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
