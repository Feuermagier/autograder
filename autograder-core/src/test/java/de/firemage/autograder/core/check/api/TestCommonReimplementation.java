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

class TestCommonReimplementation extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "common-reimplementation";

    @Test
    void testSimpleArrayCopy() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static int[] copyArray(int[] toCopy) {
                        int[] result = new int[toCopy.length];

                        for (int i = 0; i < toCopy.length; i++) {
                            result[i] = toCopy[i];
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "suggestion", "System.arraycopy(toCopy, 0, result, 0, toCopy.length)"
                    )
                )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testOperatorAssignment() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static int[] copyArray(int[] toCopy) {
                        int[] result = new int[toCopy.length];

                        for (int i = 0; i < toCopy.length; i++) {
                            result[i] += toCopy[i];
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));


        assertEquals(0, problems.size());
    }

    @Test
    void testArrayCopyWithCustomStart() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static int[] copyArray(int start, int[] toCopy) {
                        int[] result = new int[toCopy.length];

                        for (int i = start; i <= toCopy.length - 1; i++) {
                            result[i] = toCopy[i];
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "suggestion", "System.arraycopy(toCopy, start, result, start, toCopy.length - start)"
                    )
                )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testSimpleStringRepeat() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static String repeat(String s, int n) {
                        String result = "";

                        for (int i = 0; i < n; i++) {
                            result += s;
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT));

        assertEquals(1, problems.size());
        assertEquals(ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "suggestion", "result += s.repeat(n)"
                    )
                )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testStringRepeatWithCustomStart() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static String repeat(String s, int n, int start) {
                        String result = "";

                        for (int i = start; i <= n; i++) {
                            result += s;
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT));

        assertEquals(1, problems.size());
        assertEquals(ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "suggestion", "result += s.repeat((n + 1) - start)"
                    )
                )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testDoubleArrayCopy() throws LinterException, IOException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "MatrixUtils",
            """
                public class MatrixUtils {
                    public static int[][] copyMatrix(int[][] matrix) {
                        int n = matrix.length;
                        int m = matrix[0].length;

                        int[][] result = new int[n][m];
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < m; j++) { // Not Ok (= System.arraycopy(matrix[i], 0, result[i], 0, m))
                                result[i][j] = matrix[i][j];
                            }
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "suggestion", "System.arraycopy(matrix[i], 0, result[i], 0, m)"
                    )
                )),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }
}
