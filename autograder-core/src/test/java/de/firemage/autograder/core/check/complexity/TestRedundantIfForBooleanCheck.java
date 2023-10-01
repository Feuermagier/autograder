package de.firemage.autograder.core.check.complexity;

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

class TestRedundantIfForBooleanCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.REDUNDANT_IF_FOR_BOOLEAN);

    void assertEqualsRedundant(Problem problem, String suggestion) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "common-reimplementation",
                Map.of("suggestion", suggestion)
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testAssignment() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public void doA(int a) {
                        boolean b = false;

                        if (a == 0) {
                            b = true;
                        } else {
                            b = false;
                        }
                    }

                    public void doB(int a) {
                        boolean b = false;

                        if (a == 1) {
                            b = false;
                        } else {
                            b = true;
                        }
                    }

                    public void doC(int a) {
                        boolean b = false;

                        if (!(a == 2)) {
                            b = true;
                        } else {
                            b = false;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);


        assertEqualsRedundant(problems.next(), "b = a == 0");
        assertEqualsRedundant(problems.next(), "b = a != 1");
        assertEqualsRedundant(problems.next(), "b = !(a == 2)");

        problems.assertExhausted();
    }

    // if (a) { return true; } else { return false; }
    // if (!a) { return true; } else { return false; }
    @Test
    void testReturnIfElse() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public boolean doA(int a) {
                        if (a == 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    public boolean doB(int a) {
                        if (a == 1) {
                            return false;
                        } else {
                            return true;
                        }
                    }

                    public boolean doC(int a) {
                        if (!(a == 2)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);


        assertEqualsRedundant(problems.next(), "return a == 0");
        assertEqualsRedundant(problems.next(), "return a != 1");
        assertEqualsRedundant(problems.next(), "return !(a == 2)");

        problems.assertExhausted();
    }

    // if (a) { return true; } return false;
    // if (!a) { return true; } return false;
    @Test
    void testReturnIfImplicitElse() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public boolean doA(int a) {
                        if (a == 0) {
                            return true;
                        }

                        return false;
                    }
                    
                    public boolean doB(int a) {
                        if (a == 1) {
                            return false;
                        }

                        return true;
                    }
                }
                """
        ), PROBLEM_TYPES);


        assertEqualsRedundant(problems.next(), "return a == 0");
        assertEqualsRedundant(problems.next(), "return a != 1");

        problems.assertExhausted();
    }

    // if (a) { return true; } else if (b) { return true; } else { return false; }
    @Test
    void testReturnIfElseIfElse() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public boolean doA(int a) {
                        if (a == 0) {
                            return true;
                        } else if (a == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    
                    public boolean doB(int a) {
                        if (a == 2) {
                            return true;
                        } else if (a == 3) {
                            return true;
                        }

                        return false;
                    }
                    
                    public boolean doC(int a) {
                        if (a == 4) {
                            return true;
                        } else if (a == 5) {
                            return false;
                        }

                        return false;
                    }
                }
                """
        ), PROBLEM_TYPES);

        // NOTE: it ignores the first if, only the else if else are checked
        assertEqualsRedundant(problems.next(), "return a == 1");
        // assertEqualsRedundant(problems.next(), "return a == 3");

        problems.assertExhausted();
    }

    @Test
    void testIfWithExtraStatement() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public boolean doA(int a) {
                        if (a == 0) { //# ok
                            System.out.println("a == 0");
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testPartialLiteralReturn() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    boolean doA(int a) {
                        if (a == 0) {
                            return a == 1;
                        }

                        return true;
                    }

                    boolean doB(int a) {
                        if (a == 2) {
                            return true;
                        }

                        return a == 3;
                    }

                    boolean doC(int a) {
                        if (a == 4) {
                            return false;
                        }

                        return a == 5;
                    }
                    
                    boolean doD(int a) {
                        if (a == 6) {
                            return a == 7;
                        }

                        return false;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertEqualsRedundant(problems.next(), "return ((a == 0) && (a == 1)) || (a != 0)");
        assertEqualsRedundant(problems.next(), "return (a == 2) || (a == 3)");
        assertEqualsRedundant(problems.next(), "return (a != 4) && (a == 5)");
        assertEqualsRedundant(problems.next(), "return (a == 6) && (a == 7)");

        problems.assertExhausted();
    }

    @Test
    void testNonLiteralReturn() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    boolean doA(int a) {
                        if (a == 0) {
                            return a == 1;
                        }

                        return a == 2;
                    }

                    boolean doB(int a) {
                        if (a == 3) {
                            return a == 4;
                        } else {
                            return a == 5;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertEqualsRedundant(problems.next(), "return ((a == 0) && (a == 1)) || (a == 2)");
        assertEqualsRedundant(problems.next(), "return ((a == 3) && (a == 4)) || (a == 5)");

        problems.assertExhausted();
    }

    @Test
    void testIgnoreInvalidIfWithValidElseIfElse() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public boolean doA(int a) {
                        if (a == 0) {
                            // because of the print, one can not simply remove the if
                            System.out.println("a == 0");
                            return true;
                        } else if (a == 1) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        // the else-if-else can be replaced with a return, because the if is terminated by a return
        assertEqualsRedundant(problems.next(), "return a == 1");
        problems.assertExhausted();
    }

    @Test
    void testIfWithoutBody() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    public boolean doA(int a) {
                        if (a == 0);

                        return true;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
