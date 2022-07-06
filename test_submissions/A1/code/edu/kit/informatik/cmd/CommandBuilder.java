package edu.kit.informatik.cmd;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Builder class for command RegEx strings. You should not create an instance of this class; all methods are static.
 *
 * @author ukidf
 * @version 1.0
 */
public final class CommandBuilder {

    private CommandBuilder() {
    }

    /**
     * Starts the building process of a command with any parameter count
     *
     * @param command The name of this command without any parameters
     * @return A builder object for further processing of parameters
     */
    public static CommandBuilderParameter command(String command) {
        return new CommandBuilderParameter(command.trim());
    }

    /**
     * An intermediate parameter object used in the build process
     */
    public static final class CommandBuilderParameter {
        private static final String UNSIGNED_INT_MATCHER = "[+]?\\d+";
        private static final String UNSIGNED_DOUBLE_MATCHER = "[0-9]*\\.?[0-9]+";
        private static final String OPTIONAL_SIGN = "[-+]?";
        private static final String BOOLEAN_MATCHER = "true|false";
        private static final String WORD_MATCHER = "[a-zA-Z0-9-_]+";

        private String regex;

        private CommandBuilderParameter(String command) {
            this.regex = command;
        }

        /**
         * Adds a new parameter
         *
         * @param regex The Regular expression this parameters must match
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter addParameter(String regex) {
            this.regex += "(" + regex + ")";
            return this;
        }

        /**
         * Adds a new parameter as a signed integer. No range check is provided.
         * You can assume that this parameter is parsable by Integer.parseInt
         *
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter addIntParameter() {
            return addParameter(OPTIONAL_SIGN + UNSIGNED_INT_MATCHER);
        }

        /**
         * Adds a new parameter as a signed floating point number. No range check is provided.
         * You can assume that this parameter is parsable by Double.parseDouble
         *
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter addSignedDoubleParameter() {
            return addParameter(OPTIONAL_SIGN + UNSIGNED_DOUBLE_MATCHER);
        }

        /**
         * Adds a new parameter as a word containing upper and lower case letters, numbers and '_'.
         *
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter addStringParameter() {
            return addParameter(WORD_MATCHER);
        }

        /**
         * Adds a new parameter as a point of the format '(x,y)'.
         *
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter addPointParameter() {
            sep("\\(");
            addIntParameter();
            sep(",");
            addIntParameter();
            sep("\\)");
            return this;
        }

        /**
         * Adds a new parameter that can have the name of the enum constants in enumType as lower case strings.
         *
         * Idea from https://stackoverflow.com/questions/2205891/iterate-enum-values-using-java-generics
         *
         * @param enumType The enum's class
         * @param <E> The enum itself
         * @return A builder object for further processing of parameters
         */
        public <E extends Enum<E>> CommandBuilderParameter addEnumParameter(Class<E> enumType) {
            addParameter(Arrays.stream(enumType.getEnumConstants())
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.joining("|")));
            return this;
        }

        /**
         * Adds a new parameter as a boolean value that can be either 'true' or 'false'.
         *
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter addBooleanParameter() {
            return addParameter(BOOLEAN_MATCHER);
        }

        /**
         * Adds the given string to the RegEx without creating a new regex group,
         * so this string cannot be accessed after compiling the RegEx.
         *
         * @param separator The separator which should be added
         * @return A builder object for further processing of parameters
         */
        public CommandBuilderParameter sep(String separator) {
            regex += separator;
            return this;
        }

        /**
         * Build the RegEx string from your command name and all parameters.
         *
         * @return An RegEx string
         */
        public String build() {
            return regex;
        }
    }
}
