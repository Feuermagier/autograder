package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.AbstractCheckTest;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class TestVariablesHaveDescriptiveNamesCheck extends AbstractCheckTest {
    private static final List<ProblemType> PROBLEM_TYPES = List.of(
        ProblemType.SINGLE_LETTER_LOCAL_NAME,
        ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE,
        ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME,
        ProblemType.SIMILAR_IDENTIFIER,
        ProblemType.IDENTIFIER_REDUNDANT_NUMBER_SUFFIX
    );

    @Test
    void testNonUnicodeName() throws IOException, LinterException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
            class Test {
                enum Monat {
                    Januar,
                    Februar,
                    März,
                    April,
                    Mai,
                    August,
                    September,
                    Oktober,
                    November,
                    Dezember
                }
            
                class Kalenderdatum {
                    private double jahr;
                    private Monat monat;
                    private byte tag;
                }
            }
            """
        ), PROBLEM_TYPES);

        problems.assertExhausted();
    }
}
