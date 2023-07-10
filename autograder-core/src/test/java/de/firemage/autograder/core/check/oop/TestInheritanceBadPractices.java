package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.file.StringSourceInfo;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestInheritanceBadPractices extends AbstractCheckTest {
    @Test
    void testCompositionMessage() throws LinterException, IOException {
        List<Problem> problems = this.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
            public class Test {}

            abstract class ExampleParent { // Not Ok (composition over inheritance)
                private String field;
                protected ExampleParent() {
                    this.field = "test";
                }
            }

            class Subclass extends ExampleParent {
                public Subclass() {
                    super();
                }
            }
            """), List.of(ProblemType.COMPOSITION_OVER_INHERITANCE));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.COMPOSITION_OVER_INHERITANCE, problems.get(0).getProblemType());
        assertEquals(this.linter.translateMessage(new LocalizedMessage("composition-over-inheritance", Map.of("suggestion", "ExampleParent exampleParent()"))), this.linter.translateMessage(problems.get(0).getExplanation()));
    }
}
