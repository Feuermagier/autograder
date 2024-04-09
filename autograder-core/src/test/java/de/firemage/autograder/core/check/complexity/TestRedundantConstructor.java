package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestRedundantConstructor extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.REDUNDANT_DEFAULT_CONSTRUCTOR);

    @Test
    void testRedundantConstructor() throws IOException, LinterException {
        assertRedundant(
            "Test",
            """
                public class Test {
                    public Test() {
                    
                    }
                }
                """
        );
    }

    @Test
    void testRedundantConstructorSuper() throws IOException, LinterException {
        assertRedundant(
            "Test",
            """
                public class Test {
                    public Test() {
                        super();
                    }
                }
                """
        );
    }

    @Test
    void testRedundantConstructorInnerClassSuper() throws IOException, LinterException {
        assertRedundant(
            "Test",
            """
                public class Test {
                    public class Inner {
                        public Inner() {
                            super(/* test */);
                        }
                    }
                }
                """
        );
    }

    @Test
    void testRedundantEnumConstructor() throws IOException, LinterException {
        assertRedundant(
            "TestEnum",
            """
                public enum TestEnum {
                    ;
                    private TestEnum() {
                    }
                }
                """
        );
    }

    @Test
    void testRedundantEnumConstructorNoModifier() throws IOException, LinterException {
        assertRedundant(
            "TestEnum",
            """
                public enum TestEnum {
                    ;
                    TestEnum() {
                    }
                }
                """
        );
    }

    @Test
    void testRedundantRecordCompactConstructor() throws IOException, LinterException {
        assertRedundant(
            "Test",
            """
                record Test() {
                    Test {
                    }
                }
                """
        );
    }

    @Test
    void testRedundantEmptyRecordNormalConstructor() throws IOException, LinterException {
        assertRedundant(
            "Test",
            """
                public record Test() {
                    public Test() {
                    }
                }
                """
        );
    }

    @Test
    void testRedundantRecordNormalConstructor() throws IOException, LinterException {
        assertRedundant(
            "Test",
            """
                public record Test(int a, int b) {
                    public Test(int a, int b) {
                        this.b = b;
                        this.a = a;
                    }
                }
                """
        );
    }

    @Test
    void testNotRedundantImplicitConstructor() throws IOException, LinterException {
        assertNotRedundant(
            "Test",
            """
                public class Test {
                }
                """
        );
    }

    @Test
    void testNotRedundantImplicitRecordConstructor() throws IOException, LinterException {
        assertNotRedundant(
            "TestRecord",
            """
                record TestRecord() {
                }
                """
        );
    }

    @Test
    void testNotRedundantRecordNormalConstructor() throws IOException, LinterException {
        assertNotRedundant(
            "Test",
            """
                public record Test(int a, int b) {
                    public Test(int a, int b) {
                        this.a = b;
                        this.b = a;
                    }
                }
                """
        );
    }

    @Test
    void testNotRedundantRecordNormalConstructor2() throws IOException, LinterException {
        assertNotRedundant(
            "Test",
            """
                public record Test(int a, int b) {
                    public Test(int a, int b) {
                        this.a = a;
                        this.b = b;
                        System.out.println();
                    }
                }
                """
        );
    }

    @Test
    void testNotRedundantConstructorVisibility() throws IOException, LinterException {
        assertNotRedundant(
            "Test",
            """
                public class Test {
                    protected Test() {
                    }
                }
                """
        );
    }

    @Test
    void testNotRedundantBody() throws IOException, LinterException {
        assertNotRedundant(
            "Test",
            """
                public class Test {
                    public Test() {
                        System.out.println();
                    }
                }
                """
        );
    }

    @Test
    void testNotRedundantConstructorSuperArgs() throws IOException, LinterException {
        assertRedundant(
            StringSourceInfo.fromSourceStrings(JavaVersion.JAVA_17, Map.of(
                "Base",
                """
                    public class Base {
                        public Base(String foo) {
                        }
                    }
                    """,
                "Child",
                """
                    public class Child extends Base {
                        public Child() {
                            super("foo");
                        }
                    }
                    """
            )),
            false
        );
    }

    private void assertRedundant(String className, String source) throws LinterException, IOException {
        assertRedundant(className, source, true);
    }

    private void assertNotRedundant(String className, String source) throws LinterException, IOException {
        assertRedundant(className, source, false);
    }

    private void assertRedundant(String className, String source, boolean redundant) throws LinterException, IOException {
        assertRedundant(StringSourceInfo.fromSourceString(JavaVersion.JAVA_17, className, source), redundant);
    }

    private void assertRedundant(SourceInfo info, boolean redundant) throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(info, PROBLEM_TYPES);
        if (redundant) {
            assertEqualsRedundant(problems.next());
        }
        problems.assertExhausted();
    }

    private void assertEqualsRedundant(Problem problem) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage("implicit-constructor-exp")),
            this.linter.translateMessage(problem.getExplanation())
        );
    }
}
