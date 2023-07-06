package de.firemage.autograder.span;

import org.junit.jupiter.api.Test;

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
}
