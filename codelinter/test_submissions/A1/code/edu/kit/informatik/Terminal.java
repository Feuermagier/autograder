package edu.kit.informatik;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class provides some simple methods for input/output from and to a terminal as well as a method to read in
 * files.
 *
 * <p><b>Never modify this class, never upload it to Praktomat.</b> This is only for your local use. If an assignment
 * tells you to use this class for input and output never use System.out, System.err or System.in in the same
 * assignment.
 *
 * @author  ITI, VeriAlg Group
 * @author  IPD, SDQ Group
 * @version 5.03, 2016/05/07
 */
public final class Terminal {

    /**
     * Reads text from the "standard" input stream, buffering characters so as to provide for the efficient reading
     * of characters, arrays, and lines. This stream is already open and ready to supply input data and corresponds
     * to keyboard input.
     */
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Private constructor to avoid object generation.
     *
     * @deprecated Utility-class constructor.
     */
    @Deprecated
    private Terminal() {
        throw new AssertionError("Utility class constructor.");
    }

    /**
     * Prints the given error-{@code message} with the prefix "{@code Error, }".
     *
     * <p>More specific, this method behaves exactly as if the following code got executed:
     * <blockquote><pre>
     * Terminal.printLine("Error, " + message);</pre>
     * </blockquote>
     *
     * @param message the error message to be printed
     * @see   #printLine(Object)
     */
    public static void printError(final String message) {
        Terminal.printLine("Error, " + message);
    }

    /**
     * Prints the string representation of an {@code Object} and then terminate the line.
     *
     * <p>If the argument is {@code null}, then the string {@code "null"} is printed, otherwise the object's string
     * value {@code obj.toString()} is printed.
     *
     * @param object the {@code Object} to be printed
     * @see   String#valueOf(Object)
     */
    public static void printLine(final Object object) {
        System.out.println(object);
    }

    /**
     * Prints an array of characters and then terminate the line.
     *
     * <p>If the argument is {@code null}, then a {@code NullPointerException} is thrown, otherwise the value of {@code
     * new String(charArray)} is printed.
     *
     * @param charArray an array of chars to be printed
     * @see   String#valueOf(char[])
     */
    public static void printLine(final char[] charArray) {
        /*
         * Note: This method's sole purpose is to ensure that the Terminal-class behaves exactly as
         * System.out regarding output. (System.out.println(char[]) calls String.valueOf(char[])
         * which itself returns 'new String(char[])' and is therefore the only method that behaves
         * differently when passing the provided parameter to the System.out.println(Object)
         * method.)
         */
        System.out.println(charArray);
    }

    /**
     * Reads a line of text. A line is considered to be terminated by any one of a line feed ('\n'), a carriage return
     * ('\r'), or a carriage return followed immediately by a linefeed.
     *
     * @return a {@code String} containing the contents of the line, not including any line-termination characters, or
     *         {@code null} if the end of the stream has been reached
     */
    public static String readLine() {
        try {
            return IN.readLine();
        } catch (final IOException e) {
            /*
             * The IOException will not occur during tests executed by the praktomat, therefore the
             * following RuntimeException does not have to get handled.
             */
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the file with the specified path and returns its content stored in a {@code String} array, whereas the
     * first array field contains the file's first line, the second field contains the second line, and so on.
     *
     * @param  path the path of the file to be read
     * @return the content of the file stored in a {@code String} array
     */
    public static String[] readFile(final String path) {
        try (final BufferedReader reader = new BufferedReader(new FileReader(path))) {
            return reader.lines().toArray(String[]::new);
        } catch (final IOException e) {
            /*
             * You can expect that the praktomat exclusively provides valid file-paths. Therefore
             * there will no IOException occur while reading in files during the tests, the
             * following RuntimeException does not have to get handled.
             */
            throw new RuntimeException(e);
        }
    }
}
