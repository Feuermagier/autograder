package de.firemage.autograder.core.check;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.file.StringSourceInfo;
import de.firemage.autograder.core.compiler.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestMergedProblems extends AbstractCheckTest {
    private static final String VARIABLE_FORMAT_STRING = "intField%d";
    TestMergedProblems() {
        super(5);
    }

    private static Stream<String> generateViolations(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> "private int %s;".formatted(VARIABLE_FORMAT_STRING.formatted(i)));
    }

    @Test
    void testMergeSingleFile() throws LinterException, IOException {
        List<String> fields = generateViolations(10).toList();

        List<Problem> problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                Map.entry(
                    "Test",
                    """
                    public class Test {
                        %s
                        public static void main(String[] args) {}
                    }
                    """.formatted(String.join("\n", fields))
                )
            )
        ), List.of(ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME));

        assertEquals(5, problems.size());

        for (int i = 0; i < 4; i++) {
            assertEquals(ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME, problems.get(i).getProblemType());
            assertEquals(
                this.linter.translateMessage(new LocalizedMessage(
                    "variable-name-type-in-name",
                    Map.of("name", VARIABLE_FORMAT_STRING.formatted(i))
                )),
                this.linter.translateMessage(problems.get(i).getExplanation())
            );
        }

        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "merged-problems",
                Map.of(
                    "message", this.linter.translateMessage(new LocalizedMessage(
                        "variable-name-type-in-name",
                        Map.of("name", VARIABLE_FORMAT_STRING.formatted(4))
                    )),
                    "locations", "L7, L8, L9, L10, L11"
                )
            )),
            this.linter.translateMessage(problems.get(4).getExplanation())
        );
    }

    @Test
    void testMergeMultiFile() throws LinterException, IOException {
        List<String> fields = generateViolations(10).toList();

        List<Problem> problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                Map.entry(
                    "Test",
                    """
                    public class Test {
                        %s
                        public static void main(String[] args) {}
                    }
                    """.formatted(String.join("\n", fields))
                ),
                Map.entry(
                    "Vector",
                    """
                    public class Vector {
                        %s
                    }
                    """.formatted(String.join("\n", fields.subList(0, 3)))
                )
            )
        ), List.of(ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME));

        assertEquals(5, problems.size());

        for (int i = 0; i < 4; i++) {
            assertEquals(ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME, problems.get(i).getProblemType());
            assertEquals(
                this.linter.translateMessage(new LocalizedMessage(
                    "variable-name-type-in-name",
                    Map.of("name", VARIABLE_FORMAT_STRING.formatted(i))
                )),
                this.linter.translateMessage(problems.get(i).getExplanation())
            );
        }

        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "merged-problems",
                Map.of(
                    "message", this.linter.translateMessage(new LocalizedMessage(
                        "variable-name-type-in-name",
                        Map.of("name", VARIABLE_FORMAT_STRING.formatted(4))
                    )),
                    "locations", "Test:(L7, L8, L9, L10, L11), Vector:(L2, L3, L4)"
                )
            )),
            this.linter.translateMessage(problems.get(4).getExplanation())
        );
    }

    /**
     * Tests that instead of 'File:(L1)' the location is 'File:L1' if there is only violation in the file
     */
    @Test
    void testMergeSingleViolationInFile() throws LinterException, IOException {
        List<String> fields = generateViolations(10).toList();

        List<Problem> problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                Map.entry(
                    "Test",
                    """
                    public class Test {
                        %s
                        public static void main(String[] args) {}
                    }
                    """.formatted(String.join("\n", fields))
                ),
                Map.entry(
                    "Vector",
                    """
                    public class Vector {
                        %s
                    }
                    """.formatted(fields.get(0))
                )
            )
        ), List.of(ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME));

        assertEquals(5, problems.size());

        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "merged-problems",
                Map.of(
                    "message", this.linter.translateMessage(new LocalizedMessage(
                        "variable-name-type-in-name",
                        Map.of("name", VARIABLE_FORMAT_STRING.formatted(4))
                    )),
                    "locations", "Test:(L7, L8, L9, L10, L11), Vector:L2"
                )
            )),
            this.linter.translateMessage(problems.get(4).getExplanation())
        );
    }
}
