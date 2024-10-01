package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.api.JavaVersion;
import de.firemage.autograder.api.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestDuplicateIfBlock extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.DUPLICATE_IF_BLOCK);

    void assertDuplicate(Problem problem, List<String> conditions) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "common-reimplementation",
                Map.of(
                    "suggestion", "if (%s) { ... }".formatted(String.join(" || ", conditions))
                )
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testMissingThen() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    void foo(int i) {
                        if (i == 0);
                    }

                    public static void main(String[] args) {}
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testIfElse() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    void foo(int i) {
                        if (i == 0) {
                            System.out.println("zero");
                            return;
                        } else {
                            if (i == 1) {
                                System.out.println("zero");
                                return;
                            }
                        }
                    }

                    public static void main(String[] args) {}
                }
                """
        ), PROBLEM_TYPES);

        assertDuplicate(problems.next(), List.of("i == 0", "i == 1"));

        problems.assertExhausted();
    }

    @Test
    void testIfElseExtraStatement() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    void foo(int i) {
                        if (i == 0) {
                            System.out.println("zero");
                            return;
                        } else {
                            if (i == 1) {
                                System.out.println("zero");
                                return;
                            }
                            
                            System.out.println("extra");
                        }
                    }

                    public static void main(String[] args) {}
                }
                """
        ), PROBLEM_TYPES);

        assertDuplicate(problems.next(), List.of("i == 0", "i == 1"));

        problems.assertExhausted();
    }


    @Test
    void testMultipleNestedIf() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    void foo(int i) {
                        if (i == 0) {
                            System.out.println("zero");
                            return;
                        } else {
                            if (i == 1) {
                                System.out.println("zero");
                                return;
                            } else {
                                if (i == 2) {
                                    System.out.println("zero");
                                    return;
                                } else {
                                    if (i == 3) {
                                        System.out.println("zero");
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    public static void main(String[] args) {}
                }
                """
        ), PROBLEM_TYPES);

        assertDuplicate(problems.next(), List.of("i == 2", "i == 3"));

        problems.assertExhausted();
    }

    @Test
    void testFollowingIf() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    void foo(int i) {
                        if (i == 0) {
                            System.out.println("zero");
                            return;
                        }
                        
                        if (i == 1) {
                            System.out.println("zero");
                            return;
                        }

                        if (i == 2) {
                            System.out.println("zero");
                            return;
                        }
                        
                        if (i == 3) {
                            System.out.println("1");
                            return;
                        }
                    }

                    public static void main(String[] args) {}
                }
                """
        ), PROBLEM_TYPES);

        assertDuplicate(problems.next(), List.of("i == 0", "i == 1", "i == 2"));

        problems.assertExhausted();
    }
}
