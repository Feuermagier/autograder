package de.firemage.autograder.core.check.general;

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

public class TestConstantNamingAndQualifierCheck extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "variable-should-be";

    @Test
    void testDefaultVisibility() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    String exampleConstant = "example";
                }
                """
        ), List.of(ProblemType.FIELD_SHOULD_BE_CONSTANT));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.FIELD_SHOULD_BE_CONSTANT, problems.get(0).getProblemType());
        assertEquals(super.linter.translateMessage(
            new LocalizedMessage(
                LOCALIZED_MESSAGE_KEY,
                Map.of(
                    "variable", "exampleConstant",
                    "suggestion", "static final String EXAMPLE_CONSTANT = \"example\""
                )
            )),
            super.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testOtherVisibility() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private final int variable = 1;
                }
                """
        ), List.of(ProblemType.FIELD_SHOULD_BE_CONSTANT));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.FIELD_SHOULD_BE_CONSTANT, problems.get(0).getProblemType());
        assertEquals(super.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "variable", "variable",
                        "suggestion", "private static final int VARIABLE = 1"
                    )
                )),
            super.linter.translateMessage(problems.get(0).getExplanation())
        );
    }
}
