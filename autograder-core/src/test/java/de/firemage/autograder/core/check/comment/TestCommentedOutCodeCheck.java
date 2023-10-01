package de.firemage.autograder.core.check.comment;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCommentedOutCodeCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.COMMENTED_OUT_CODE);

    void assertEqualsCode(Problem problem) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage("commented-out-code")),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testInlineCode() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(
            StringSourceInfo.fromSourceString(
                JavaVersion.JAVA_17,
                "Test",
                """
                    public class Test {
                        // int a = b;

                        // if (a) {
                        //    ...
                        // }
                    }
                    """
            ),
            PROBLEM_TYPES
        );

        assertEqualsCode(problems.next());
        assertEqualsCode(problems.next());
        assertEqualsCode(problems.next());

        problems.assertExhausted();
    }

    @Test
    void testMultilineCode() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(
            StringSourceInfo.fromSourceString(
                JavaVersion.JAVA_17,
                "Test",
                """
                    public class Test {
                        /*
                        int a = b;
                        
                        if (a == 3) {
                            System.out.println("a is 3");
                        }
                         */
                    }
                    """
            ),
            PROBLEM_TYPES
        );

        assertEqualsCode(problems.next());

        problems.assertExhausted();
    }
}
