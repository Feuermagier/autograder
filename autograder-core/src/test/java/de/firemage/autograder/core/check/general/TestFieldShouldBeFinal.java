package de.firemage.autograder.core.check.general;

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

class TestFieldShouldBeFinal extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.FIELD_SHOULD_BE_FINAL);

    void assertFinal(Problem problem, String name) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "field-should-be-final",
                Map.of("name", name)
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testMultipleAssignments() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                class Test {
                    String value;

                    Test() {
                        this.value = "Hello World";
                    }

                    void foo() {
                        this.value = "Value";
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testAssignedOnlyInConstructor() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                class Test {
                    String value;

                    Test() {
                        this.value = "Hello World";
                    }

                    @Override
                    public String toString() {
                        return this.value;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertFinal(problems.next(), "value");

        problems.assertExhausted();
    }

    @Test
    void testInitializedAndAssignedInConstructor() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                class Test {
                    int value = 1;

                    Test() {
                        this.value = 2;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testAlreadyFinal() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                class Test {
                    final int value;

                    Test() {
                        this.value = 2;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testPartialConstructorInit() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "A",
            """
                abstract class A {
                    private String value = null;

                    protected A() {

                    }

                    protected A(String value) {
                        this.value = value;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testStaticVariableInlineAssignedInConstructor() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "User",
            """
                class User {
                    static int nextId;
                    final int id;

                    User() {
                        this.id = nextId++;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testStaticVariableOnlyInitialized() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "User",
            """
                class User {
                    static int nextId = 1;
                    final int id;

                    User() {
                        this.id = nextId;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertFinal(problems.next(), "nextId");

        problems.assertExhausted();
    }

    @Test
    void testStaticVariableAssignedConstantInConstructor() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "User",
            """
                class User {
                    static int nextId;
                    final int id;

                    User() {
                        nextId = 1;
                        this.id = nextId;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

    @Test
    void testStaticVariableDynamicAssignment() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "User",
            """
                class User {
                    static int nextId;
                    final int id;

                    User(int next) {
                        nextId = next;
                        this.id = next;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
