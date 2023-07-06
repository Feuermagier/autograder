package de.firemage.autograder.core.span;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.compiler.JavaVersion;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.core.file.StringSourceInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestFormatter {

    @Test
    void testInlineRenderWithNumbers() {
        String sourceCode = """
        public class Test {
            public static void main(String[] args) {
                System.out.println(1);
            }
        }
        """;

        Span span = new Span(new Position(0, 13), new Position(0, 17));
        Formatter formatter = new Formatter(
            "\n",
            new Highlight(span, Optional.of("this is a label"), Style.ERROR),
            null
        );

        assertEquals(
                """
                 1 | public class Test {
                   |              ^^^^ this is a label
                 2 |     public static void main(String[] args) {
                 3 |         System.out.println(1);
                 4 |     }
                 5 | }\
                """,
            formatter.render(Text.fromString(0, sourceCode))
        );
    }

    @Test
    void testMultilineWithNumbers() {
        String sourceCode = """
        public class Test {
            public static void main(String[] args) {
                System.out.println(1);
            }
        }
        """;

        Span span = new Span(new Position(0, 19), new Position(4, 1));
        Formatter formatter = new Formatter(
            "\n",
            new Highlight(span, Optional.of("this is a multiline span"), Style.ERROR),
            null
        );

        assertEquals(
            """
                 1 |   public class Test {
                   |  ___________________^
                 2 | |     public static void main(String[] args) {
                 3 | |         System.out.println(1);
                 4 | |     }
                 5 | | }
                   | |_^ this is a multiline span\
                """,
            formatter.render(Text.fromString(0, sourceCode))
        );
    }

    @Test
    void testMultilineWithIntersectingViewbox() {
        String sourceCode = """
        public class Test {
            public static void main(String[] args) {
                System.out.println(1);
            }
        }
        """;

        Span span = new Span(new Position(0, 19), new Position(4, 1));
        Formatter formatter = new Formatter(
            "\n",
            new Highlight(span, Optional.of("this is a multiline span"), Style.ERROR),
            2
        );

        assertEquals(
            """
                 1 |   public class Test {
                   |  ___________________^
                 2 | |     public static void main(String[] args) {
                 3 | |         System.out.println(1);
                 4 | |     }
                 5 | | }
                   | |_^ this is a multiline span\
                """,
            formatter.render(Text.fromString(0, sourceCode))
        );
    }

    @Test
    void testInlineWithIntersectingViewbox() {
        String sourceCode = """
        public class Test {
            public static void main(String[] args) {
                System.out.println(1);
            }
        }
        """;

        Span span = new Span(new Position(0, 13), new Position(0, 17));
        Formatter formatter = new Formatter(
            "\n",
            new Highlight(span, Optional.of("this is a label"), Style.ERROR),
            4
        );

        assertEquals(
            """
             1 | public class Test {
               |              ^^^^ this is a label
             2 |     public static void main(String[] args) {
             3 |         System.out.println(1);
             4 |     }
             5 | }\
            """,
            formatter.render(Text.fromString(0, sourceCode))
        );
    }

    @Test
    void testInlineViewbox() {
        String sourceCode = """
        public class Test {
            public static void main(String[] args) {
                System.out.println(1);
            }
        }
        """;

        Span span = new Span(new Position(0, 13), new Position(0, 17));
        Formatter formatter = new Formatter(
            "\n",
            new Highlight(span, Optional.of("this is a label"), Style.ERROR),
            2
        );

        assertEquals(
            """
             1 | public class Test {
               |              ^^^^ this is a label
             2 |     public static void main(String[] args) {
             3 |         System.out.println(1);\
            """,
            formatter.render(Text.fromString(0, sourceCode))
        );
    }

    @Test
    void testMultilineViewbox() {
        String sourceCode = """
        public class Test {
            public static void main(String[] args) {
                System.out.println(1);
            }
        }
        """;

        Span span = new Span(new Position(0, 19), new Position(4, 1));
        Formatter formatter = new Formatter(
            "\n",
            new Highlight(span, Optional.of("this is a multiline span"), Style.ERROR),
            1
        );

        assertEquals(
            """
             1 |   public class Test {
               |  ___________________^
             2 | |     public static void main(String[] args) {
            .. | |
             4 | |     }
             5 | | }
               | |_^ this is a multiline span\
            """,
            formatter.render(Text.fromString(0, sourceCode))
        );
    }

    @Test
    @Disabled("Requires more work to correctly span the method")
    void testSpanMethod() {
        Text sourceCode = Text.fromString(0, """
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
        }""");

        SourceInfo sourceInfo = StringSourceInfo.fromSourceString(JavaVersion.JAVA_17, "MatrixUtils", sourceCode.text());

        CtClass<?> matrixUtils = Launcher.parseClass(sourceCode.text());

        CtMethod<?> ctMethod = matrixUtils.getMethodsByName("copyMatrix").get(0);

        System.out.println(ctMethod.getPosition().isValidPosition());
        System.out.println("L%d:%d - L%d:%d".formatted(
            ctMethod.getPosition().getLine(),
            ctMethod.getPosition().getColumn(),
            ctMethod.getPosition().getEndLine(),
            ctMethod.getPosition().getEndColumn()
        ));

        CodePosition codePosition = CodePosition.fromSourcePosition(
            ctMethod.getPosition(),
            ctMethod,
            sourceInfo
        );

        Formatter formatter = new Formatter(
            "\n",
            new Highlight(Span.of(codePosition), Optional.of("this method is unused"), Style.ERROR),
            1
        );

        assertEquals(
            """
              1 |     public static int[][] copyMatrix(int[][] matrix) {
                |                           ^^^^^^^^^^ this method is unused
              2 |         int n = matrix.length;
              3 |         int m = matrix[0].length;
              4 |
              5 |         int[][] result = new int[n][m];
              6 |         for (int i = 0; i < n; i++) {
              7 |             for (int j = 0; j < m; j++) { // Not Ok (= System.arraycopy(matrix[i], 0, result[i], 0, m))
              8 |                 result[i][j] = matrix[i][j];
              9 |             }
             10 |         }
             11 |
             12 |         return result;
             13 |     }\
            """,
            formatter.render(sourceCode)
        );
    }
}
