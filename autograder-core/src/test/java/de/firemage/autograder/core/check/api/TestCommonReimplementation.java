package de.firemage.autograder.core.check.api;

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

class TestCommonReimplementation extends AbstractCheckTest {
    private static final String LOCALIZED_MESSAGE_KEY = "common-reimplementation";

    private void assertEqualsReimplementation(Problem problem, String suggestion) {
        assertEquals(
            this.linter.translateMessage(
                new LocalizedMessage(
                    LOCALIZED_MESSAGE_KEY,
                    Map.of(
                        "suggestion", suggestion
                    )
                )),
            this.linter.translateMessage(problem.getExplanation())
        );
    }

    @Test
    void testSimpleArrayCopy() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static int[] copyArray(int[] toCopy) {
                        int[] result = new int[toCopy.length];

                        for (int i = 0; i < toCopy.length; i++) {
                            result[i] = toCopy[i];
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));

        assertEqualsReimplementation(problems.next(), "System.arraycopy(toCopy, 0, result, 0, toCopy.length)");
        problems.assertExhausted();
    }

    @Test
    void testOperatorAssignment() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static int[] copyArray(int[] toCopy) {
                        int[] result = new int[toCopy.length];

                        for (int i = 0; i < toCopy.length; i++) {
                            result[i] += toCopy[i];
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));

        problems.assertExhausted();
    }

    @Test
    void testArrayCopyWithCustomStart() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                public class Test {
                    private static int[] copyArray(int start, int[] toCopy) {
                        int[] result = new int[toCopy.length];

                        for (int i = start; i <= toCopy.length - 1; i++) {
                            result[i] = toCopy[i];
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));

        assertEqualsReimplementation(
            problems.next(),
            "System.arraycopy(toCopy, start, result, start, toCopy.length - start)"
        );
        problems.assertExhausted();
    }

    @Test
    void testDoubleArrayCopy() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "MatrixUtils",
            """
                public class MatrixUtils {
                    public static int[][] copyMatrix(int[][] matrix) {
                        int n = matrix.length;
                        int m = matrix[0].length;

                        int[][] result = new int[n][m];
                        for (int i = 0; i < n; i++) {
                            for (int j = 0; j < m; j++) { // Not Ok (= System.arraycopy(matrix[i], 0, result[i], 0, m))
                                result[i][j] = matrix[i][j];
                            }
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY));

        assertEqualsReimplementation(problems.next(), "System.arraycopy(matrix[i], 0, result[i], 0, m)");
        problems.assertExhausted();
    }

    @Test
    void testAddAllArray() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                import java.util.Collection;
                import java.util.ArrayList;

                public class Main {
                    public static <T> Collection<T> toCollection(T[] array) {
                        Collection<T> result = new ArrayList<>();
                        
                        for (T element : array) {
                            result.add(element);
                        }
                        
                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL));

        assertEqualsReimplementation(problems.next(), "result.addAll(Arrays.asList(array))");
        problems.assertExhausted();
    }

    @Test
    void testAddAllCollection() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                import java.util.Collection;
                import java.util.ArrayList;

                public class Main {
                    public static <T> Collection<T> toCollection(Iterable<T> input) {
                        Collection<T> result = new ArrayList<>();
                        
                        for (T element : input) {
                            result.add(element);
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL));

        assertEqualsReimplementation(problems.next(), "result.addAll(input)");
        problems.assertExhausted();
    }

    @Test
    void testAddAllCast() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                import java.util.Collection;
                import java.util.ArrayList;

                public class Main {
                    public static <T, U> Collection<U> toCollection(Iterable<T> input) {
                        Collection<U> result = new ArrayList<>();
                        
                        for (T element : input) {
                            result.add((U) element);
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ADD_ALL));

        problems.assertExhausted();
    }


    @Test
    void testArraysFill() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Main",
            """
                public class Main {
                    private static final String INITIAL_VALUE = "X";

                    public static void init(String[] array) {
                        for (int i = 0; i < array.length; i++) {
                            array[i] = INITIAL_VALUE;
                        }
                        
                        for (int i = 1; i < array.length; i++) {
                            array[i] = INITIAL_VALUE;
                        }
                        
                        for (int i = 0; i < array.length; i++) {
                            array[i] = INITIAL_VALUE + i; // ignored because it uses i
                        }
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL));

        assertEqualsReimplementation(problems.next(), "Arrays.fill(array, INITIAL_VALUE)");
        assertEqualsReimplementation(problems.next(), "Arrays.fill(array, 1, array.length, INITIAL_VALUE)");
        problems.assertExhausted();
    }

    // See https://github.com/Feuermagier/autograder/issues/245
    @Test
    void testArraysFillMutableClass() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.ArrayList;
                import java.util.List;

                public class Test {
                    public record Cell(List<String> list) {
                        public Cell() {
                            this(new ArrayList<>());
                        }

                        public void add(String string) { this.list.add(string); }
                    }

                    public static Cell[] createCells(int n) {
                        Cell[] result = new Cell[n];

                        for (int i = 0; i < result.length; i++) {
                            result[i] = new Cell();
                        }

                        return result;
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL));

        problems.assertExhausted();
    }

    @Test
    void testArraysFillRecursiveType() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceStrings(
            JavaVersion.JAVA_17,
            Map.ofEntries(
                Map.entry(
                    "Main",
                    """
                    class PlayingFieldEntry {
                        static final PlayingFieldEntry FREE = new PlayingFieldEntry();
                    }

                    public class Main {
                        public static void main(String[] args) {
                            PlayingFieldEntry[] field = new PlayingFieldEntry[1];

                            for (int i = 0; i < field.length; i++) {
                                field[i] = PlayingFieldEntry.FREE;
                            }
                        }
                    }
                    """
                )
            )
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ARRAYS_FILL));

        assertEqualsReimplementation(problems.next(), "Arrays.fill(field, PlayingFieldEntry.FREE)");

        problems.assertExhausted();
    }

    @Test
    void testEnumValuesListing() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.HashSet;
                import java.util.Set;
                import java.util.ArrayList;
                import java.util.List;

                enum Fruit {
                    APPLE, BANANA, CHERRY;
                }

                public class Test {
                    private static final List<Fruit> ORDERED_LIST_FRUITS = List.of(Fruit.APPLE, Fruit.BANANA, Fruit.CHERRY);
                    private static final List<Fruit> UNORDERED_LIST_FRUITS = List.of(Fruit.BANANA, Fruit.APPLE, Fruit.CHERRY);
                    private static final Set<Fruit> UNORDERED_SET_FRUITS = Set.of(Fruit.BANANA, Fruit.APPLE, Fruit.CHERRY);
                    private static final Set<Fruit> ORDERED_SET_FRUITS = Set.of(Fruit.APPLE, Fruit.BANANA, Fruit.CHERRY);
                    
                    private static final Fruit[] ORDERED_ARRAY_FRUITS = new Fruit[] {Fruit.APPLE, Fruit.BANANA, Fruit.CHERRY};
                    private static final Fruit[] UNORDERED_ARRAY_FRUITS = {Fruit.BANANA, Fruit.APPLE, Fruit.CHERRY};
                    
                    private static final Fruit[] DUPLICATE_ARRAY = {Fruit.APPLE, Fruit.BANANA, Fruit.CHERRY, Fruit.CHERRY};
                    private static final List<Fruit> DUPLICATE_LIST = List.of(Fruit.APPLE, Fruit.BANANA, Fruit.CHERRY, Fruit.CHERRY);
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES));

        assertEqualsReimplementation(problems.next(), "List.of(Fruit.values())");
        assertEqualsReimplementation(problems.next(), "Set.of(Fruit.values())");
        assertEqualsReimplementation(problems.next(), "Set.of(Fruit.values())");
        assertEqualsReimplementation(problems.next(), "Arrays.copyOf(Fruit.values(), Fruit.values().length)");
        problems.assertExhausted();
    }

    @Test
    void testSubList() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                import java.util.ArrayList;
                import java.util.List;

                public class Test {
                    public static <T> void printList(List<T> list, int start, int end) {
                        for (int i = start; i < end; i++) {
                            System.out.println(list.get(i));
                        }
                    }
                    
                    public static void printRawList(List list, int start, int end) {
                        for (int i = start; i < end; i++) {
                            System.out.println(list.get(i));
                        }
                    }
                    
                    public static void printList2(List<Integer> list, int start, int end) {
                        for (int i = start; i < end; i++) {
                            System.out.println(list.get(i));
                        }
                    }
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_SUBLIST));

        assertEqualsReimplementation(problems.next(), "for (T value : list.subList(start, end)) { ... }");
        assertEqualsReimplementation(problems.next(), "for (Object value : list.subList(start, end)) { ... }");
        assertEqualsReimplementation(problems.next(), "for (int value : list.subList(start, end)) { ... }");
        problems.assertExhausted();
    }

    @Test
    void testEnumValuesListingWithNull() throws LinterException, IOException {
        ProblemIterator problems = this.checkIterator(StringSourceInfo.fromSourceString(
            JavaVersion.JAVA_17,
            "Test",
            """
                enum Fruit {
                    APPLE, BANANA, CHERRY;
                }

                public class Test {
                    private static final Fruit[] FRUITS = { Fruit.APPLE, Fruit.BANANA, Fruit.CHERRY, null };
                }
                """
        ), List.of(ProblemType.COMMON_REIMPLEMENTATION_ADD_ENUM_VALUES));

        problems.assertExhausted();
    }
}
