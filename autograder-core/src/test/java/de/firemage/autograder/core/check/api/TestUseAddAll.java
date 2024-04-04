package de.firemage.autograder.core.check.api;

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

class TestUseAddAll extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL);

    private void assertEqualsReimplementation(Problem problem, String suggestion) {
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        "suggestion", suggestion
                    )
                )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testAddAllArray() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                import java.util.Collection;
                import java.util.ArrayList;

                public class Main {
                    public static <T> Collection<T> toCollection(T[] array) {
                        Collection<T> result = new ArrayList<>();
                        
                        for (T element : array) {
                            result.add(element);
                        }
                        
                        return result;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertEqualsReimplementation(problems.next(), "result.addAll(Arrays.asList(array))");
        problems.assertExhausted();
    }

    @Test
    void testAddAllCollection() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                import java.util.Collection;
                import java.util.ArrayList;

                public class Main {
                    public static <T> Collection<T> toCollection(Iterable<T> input) {
                        Collection<T> result = new ArrayList<>();
                        
                        for (T element : input) {
                            result.add(element);
                        }

                        return result;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertEqualsReimplementation(problems.next(), "result.addAll(input)");
        problems.assertExhausted();
    }

    @Test
    void testAddAllCast() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                import java.util.Collection;
                import java.util.ArrayList;

                public class Main {
                    public static <T, U> Collection<U> toCollection(Iterable<T> input) {
                        Collection<U> result = new ArrayList<>();
                        
                        for (T element : input) {
                            result.add((U) element);
                        }

                        return result;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
