package de.firemage.autograder.core.check.structure;

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

class TestDuplicateCode extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.DUPLICATE_CODE);

    void assertDuplicateCode(Problem problem, String left, String right) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "duplicate-code",
                Map.of(
                    "left", left,
                    "right", right
                )
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testDuplicateDifferingNonConstantExpression() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "StringUtils",
            """
                public class StringUtils {
                    public static String ceasarEncryption(String input, int key) {
                        char[] inputArray = input.toCharArray();
                        for (int i = 0; i < inputArray.length; i++) {
                            if (Character.isUpperCase(inputArray[i])) {
                                inputArray[i] = (char) (((inputArray[i] - 65 + key) % 26) + 65);
                            } else {
                                inputArray[i] = (char) (((inputArray[i] - 97 + key) % 26) + 97);
                            }
                        }
                        return new String(inputArray);
                    }

                    public static String ceasarDecryption(String input, int key) {
                        char[] inputArray = input.toCharArray();
                        for (int i = 0; i < inputArray.length; i++) {
                            if (Character.isUpperCase(inputArray[i])) {
                                inputArray[i] = (char) (((inputArray[i] - 65 + (26 - key)) % 26) + 65);
                            } else {
                                inputArray[i] = (char) (((inputArray[i] - 97 + (26 - key)) % 26) + 97);
                            }
                        }
                        return new String(inputArray);
                    }
                }
                // the two methods are mostly identical except for two assigned values
                // the decryption has a `26 - key` while the encryption has `key`
                // key is a param, so the new utility method could have an extra param that applies that shift
                """
        ), PROBLEM_TYPES);

        assertDuplicateCode(problems.next(), "StringUtils:3-11", "StringUtils:15-23");

        problems.assertExhausted();
    }
}
