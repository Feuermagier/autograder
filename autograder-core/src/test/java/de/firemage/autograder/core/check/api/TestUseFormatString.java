package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.StringSourceInfo;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUseFormatString extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "use-format-string";

    @Test
    void testSimpleArrayCopy() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static final int MIN_NUMBER = 1;
                    private static final int MAX_NUMBER = 3;

                    public static void validateNumber(int number) {
                        if (number < MIN_NUMBER || number > MAX_NUMBER || number % 2 == 0) {
                            throw new IllegalArgumentException("Board must be an odd number between " + MIN_NUMBER + " and " + MAX_NUMBER);
                        }
                    }
                }
                """
        ), List.of(ProblemType.USE_FORMAT_STRING));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.USE_FORMAT_STRING, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "formatted", "\"Board must be an odd number between %d and %d\".formatted(MIN_NUMBER, MAX_NUMBER)"
                    )
                )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }
}
