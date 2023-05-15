package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.StringSourceInfo;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestDefaultPackageCheck extends AbstractCheckTest {
    @Test
    void test() throws IOException, LinterException, InterruptedException {
        List<Problem> problems = super.check(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello World!");
                }
            }
            """
        ), List.of(ProblemType.DEFAULT_PACKAGE_USED));


        assertEquals(1, problems.size());
        assertEquals(ProblemType.DEFAULT_PACKAGE_USED, problems.get(0).getProblemType());
    }
}
