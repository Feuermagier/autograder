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

class TestUseSubList extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.COMMON_REIMPLEMENTATION_SUBLIST);

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
    void testSubList() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.ArrayList;
                import java.util.List;

                public class Test {
                    public static <T> void printList(List<T> list, int start, int end) {
                        for (int i = start; i < end; i++) {
                            System.out.println(list.get(i));
                        }
                    }
                    
                    public static void printRawList(List list, int start, int end) {
                        for (int i = start; i < end; i++) {
                            System.out.println(list.get(i));
                        }
                    }
                    
                    public static void printList2(List<Integer> list, int start, int end) {
                        for (int i = start; i < end; i++) {
                            System.out.println(list.get(i));
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertEqualsReimplementation(problems.next(), "for (T value : list.subList(start, end)) { ... }");
        assertEqualsReimplementation(problems.next(), "for (Object value : list.subList(start, end)) { ... }");
        assertEqualsReimplementation(problems.next(), "for (int value : list.subList(start, end)) { ... }");
        problems.assertExhausted();
    }
}
