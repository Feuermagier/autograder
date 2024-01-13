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

class TestUnnecessaryComment extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.UNNECESSARY_COMMENT);

    void assertEqualsEmpy(Problem problem) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage("unnecessary-comment-empty")),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testPlaceholder() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(
            StringSourceInfo.fromSourceString(
                JavaVersion.JAVA_17,
                "Test",
                """
                    public class Test {
                        public Test() {
                            //
                        }
                    }
                    """
            ),
            PROBLEM_TYPES
        );

        assertEqualsEmpy(problems.next());

        problems.assertExhausted();
    }

    @Test
    void testCommentSpacer() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(
            StringSourceInfo.fromSourceString(
                JavaVersion.JAVA_17,
                "Test",
                """
                    public class Test {
                        public Test() {
                            // Here is a very long explanation
                            //
                            // ^^ here the line is empty and valid for spacing
                        }
                    }
                    """
            ),
            PROBLEM_TYPES
        );

        problems.assertExhausted();
    }
}
