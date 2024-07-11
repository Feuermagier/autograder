package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.api.LinterException;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.file.StringSourceInfo;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.api.JavaVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestDefaultPackageCheck extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "default-package";

    @Test
    void test() throws IOException, LinterException {
        var problems = super.check(StringSourceInfo.fromSourceString(
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

    @Test
    void testMultipleClasses() throws IOException, LinterException {
        var problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                dummySourceEntry("com.example", "Test"),
                dummySourceEntry("", "Hello"),
                dummySourceEntry("", "World")
            )
        ), List.of(ProblemType.DEFAULT_PACKAGE_USED));

        assertEquals(1, problems.size());
        assertEquals(ProblemType.DEFAULT_PACKAGE_USED, problems.get(0).getProblemType());
        assertEquals(
            this.linter.translateMessage(new LocalizedMessage(LOCALIZED_MESSAGE_KEY, Map.of("positions", "Hello:L1, World:L1"))),
            this.linter.translateMessage(problems.get(0).getExplanation())
        );
    }

    @Test
    void testNoDefaultPackageUsed() throws LinterException, IOException {
        var problems = super.check(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                dummySourceEntry("com.example", "Test"),
                dummySourceEntry("com.example.a", "Hello"),
                dummySourceEntry("com.example.b", "World")
            )
        ), List.of(ProblemType.DEFAULT_PACKAGE_USED));

        assertEquals(0, problems.size());
    }
}
