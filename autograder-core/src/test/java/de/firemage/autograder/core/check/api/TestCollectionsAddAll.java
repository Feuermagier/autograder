package de.firemage.autograder.core.check.api;

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

class TestCollectionsAddAll extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "common-reimplementation";
    private static final List<ProblemType> PROBLEM_TYPES = List.of(ProblemType.COLLECTION_ADD_ALL, ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES);

    private void assertReimplementation(Problem problem, String suggestion) {
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
    void testListAdd() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.List;

                public class Test {
                    public void foo(List<String> list) {
                        list.add(" ");
                        list.add("a");
                        list.add("b");
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "list.addAll(List.of(\" \", \"a\", \"b\"))");

        problems.assertExhausted();
    }


    @Test
    void testEnumValuesAddAllUnorderedSet() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.HashSet;
                import java.util.Set;

                enum Fruit {
                    APPLE, BANANA, CHERRY;
                }

                public class Test {
                    public static void main(String[] args) {
                        Set<Fruit> fruits = new HashSet<>();
                        
                        fruits.add(Fruit.APPLE);
                        fruits.add(Fruit.BANANA);
                        fruits.add(Fruit.CHERRY);

                        System.out.println(fruits);
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "fruits.addAll(Arrays.asList(Fruit.values()))");
        problems.assertExhausted();
    }

    @Test
    void testEnumValuesAddAllOrderedList() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.ArrayList;
                import java.util.List;

                enum GodCard {
                    APOLLO,
                    ARTEMIS,
                    ATHENA,
                    ATLAS,
                    DEMETER,
                    HERMES;
                }

                public class Test {
                    private static List<GodCard> getAvailableCards() {
                        List<GodCard> availableCards = new ArrayList<>();

                        availableCards.add(GodCard.APOLLO);
                        availableCards.add(GodCard.ARTEMIS);
                        availableCards.add(GodCard.ATHENA);
                        availableCards.add(GodCard.ATLAS);
                        availableCards.add(GodCard.DEMETER);
                        availableCards.add(GodCard.HERMES);

                        return availableCards;
                    }

                    // NOTE: Enum.values() returns the variants in the order they are declared
                    //       For Sets this is not a problem, but for Lists the order in which add
                    //       is called is important
                    private static List<GodCard> getReversedAvailableGodCards() {
                        List<GodCard> availableCards = new ArrayList<>();

                        availableCards.add(GodCard.HERMES);
                        availableCards.add(GodCard.DEMETER);
                        availableCards.add(GodCard.ATLAS);
                        availableCards.add(GodCard.APOLLO);
                        availableCards.add(GodCard.ATHENA);
                        availableCards.add(GodCard.ARTEMIS);

                        return availableCards;
                    }
                    
                    private static List<GodCard> getAvailableCardsDuplicateMiddle() {
                        List<GodCard> availableCards = new ArrayList<>();

                        availableCards.add(GodCard.APOLLO);
                        availableCards.add(GodCard.ARTEMIS);
                        availableCards.add(GodCard.ATHENA);
                        availableCards.add(GodCard.ATLAS);
                        availableCards.add(GodCard.ATLAS);
                        availableCards.add(GodCard.DEMETER);
                        availableCards.add(GodCard.HERMES);

                        return availableCards;
                    }

                    private static List<GodCard> getAvailableCardsDuplicateEnd() {
                        List<GodCard> availableCards = new ArrayList<>();

                        availableCards.add(GodCard.APOLLO);
                        availableCards.add(GodCard.ARTEMIS);
                        availableCards.add(GodCard.ATHENA);
                        availableCards.add(GodCard.ATLAS);
                        availableCards.add(GodCard.DEMETER);
                        availableCards.add(GodCard.HERMES);
                        availableCards.add(GodCard.HERMES);

                        return availableCards;
                    }
                }
                """
        ), PROBLEM_TYPES);

        assertReimplementation(problems.next(), "availableCards.addAll(Arrays.asList(GodCard.values()))");
        assertReimplementation(problems.next(), "availableCards.addAll(List.of(GodCard.HERMES, GodCard.DEMETER, GodCard.ATLAS, GodCard.APOLLO, GodCard.ATHENA, GodCard.ARTEMIS))");
        assertReimplementation(problems.next(), "availableCards.addAll(List.of(GodCard.APOLLO, GodCard.ARTEMIS, GodCard.ATHENA, GodCard.ATLAS, GodCard.ATLAS, GodCard.DEMETER, GodCard.HERMES))");
        assertReimplementation(problems.next(), "availableCards.addAll(List.of(GodCard.APOLLO, GodCard.ARTEMIS, GodCard.ATHENA, GodCard.ATLAS, GodCard.DEMETER, GodCard.HERMES, GodCard.HERMES))");
        problems.assertExhausted();
    }
}
