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

class TestMagicString extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.MAGIC_STRING);

    void assertMagicString(Problem problem, String value) {
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(
                "magic-string",
                Map.of("value", value)
            )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testClass() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                class Test {
                    private static final String NON_MAGIC_STRING = "Hello World!"; //# ok
                    
                    public static void main(String[] args) {
                        String magicString = "Hello World"; //# not ok
                        String empty = "" + 1; //# ok
                    }
                    
                    private static void doSomething(String string) {
                        if (string.equals("value")) { //# not ok
                            throw new IllegalStateException("some error message"); //# not ok
                        }
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertMagicString(problems.next(), "Hello World");
        assertMagicString(problems.next(), "value");
        assertMagicString(problems.next(), "some error message");

        problems.assertExhausted();
    }

    @Test
    void testEnumConstructor() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                enum Test {
                    MONDAY("Monday"), //# ok
                    TUESDAY("Tuesday"), //# ok
                    WEDNESDAY("Wednesday"), //# ok
                    THURSDAY("Thursday"), //# ok
                    FRIDAY("Friday"), //# ok
                    SATURDAY("Saturday"), //# ok
                    SUNDAY("Sunday"); //# ok
                    
                    private final String name;
                    
                    Test(String name) {
                        this.name = name;
                    }
                }
                """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }

}
