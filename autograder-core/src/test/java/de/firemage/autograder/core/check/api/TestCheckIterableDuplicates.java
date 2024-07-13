package de.firemage.autograder.core.check.api;

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

class TestCheckIterableDuplicates extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "common-reimplementation";
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.COMMON_REIMPLEMENTATION_ITERABLE_DUPLICATES);

    private void assertReimplementation(AbstractProblem problem, String suggestion) {
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of("suggestion", suggestion)
                )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testReturnList() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.Set;
                import java.util.HashSet;
                import java.util.List;

                public class Test {
                    public static void main(String[] args) {
                    }
                    
                    public boolean hasNoDuplicates(List<String> list) {
                        Set<String> set = new HashSet<>();
                        for (String s : list) {
                            if (!set.add(s)) {
                                return false; //# not ok
                            }
                        }
                        return true;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "new HashSet<>(list).size() == list.size()");

        problems.assertExhausted();
    }

    @Test
    void testReturnArray() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.Set;
                import java.util.HashSet;
                import java.util.List;

                public class Test {
                    public static void main(String[] args) {
                    }
                    
                    public boolean hasNoDuplicates(String[] array) {
                        HashSet<String> set = new HashSet<>();
                        for (String s : array) {
                            if (!set.add(s)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "new HashSet<>(Arrays.asList(array)).size() == array.length");

        problems.assertExhausted();
    }

    @Test
    void testAssignmentList() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.Set;
                import java.util.HashSet;
                import java.util.List;

                public class Test {
                    public static void main(String[] args) {
                    }
                    
                    public boolean hasNoDuplicates(List<String> list) {
                        boolean hasDuplicates = false;
                        
                        Set<String> set = new HashSet<>();
                        for (String s : list) {
                            if (!set.add(s)) {
                                hasDuplicates = true;
                                break;
                            }
                        }

                        return !hasDuplicates;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "new HashSet<>(list).size() != list.size()");

        problems.assertExhausted();
    }
}
