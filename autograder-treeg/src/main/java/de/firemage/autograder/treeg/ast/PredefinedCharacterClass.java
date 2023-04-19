package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public record PredefinedCharacterClass(Type type) implements RegExNode {
    @Override
    public String toRegEx() {
        return switch (this.type) {
            case ANY -> ".";
            case DIGIT -> "\\d";
            case NON_DIGIT -> "\\D";
            case HORIZONTAL_WHITESPACE -> "\\h";
            case NON_HORIZONTAL_WHITESPACE -> "\\H";
            case WHITESPACE -> "\\s";
            case NON_WHITESPACE -> "\\S";
            case VERTICAL_WHITESPACE -> "\\v";
            case NON_VERTICAL_WHITESPACE -> "\\V";
            case WORD -> "\\w";
            case NON_WORD -> "\\W";
        };
    }

    @Override
    public void toTree(TreePrinter printer) {
        printer.addLine("Predefined Character Class (" + this.type + ")");
    }

    public enum Type {
        /**
         * Dot (.)
         */
        ANY,

        /**
         * \d
         */
        DIGIT,

        /**
         * \D
         */
        NON_DIGIT,

        /**
         * \h
         */
        HORIZONTAL_WHITESPACE,

        /**
         * \H
         */
        NON_HORIZONTAL_WHITESPACE,

        /**
         * \s
         */
        WHITESPACE,

        /**
         * \S
         */
        NON_WHITESPACE,

        /**
         * \v
         */
        VERTICAL_WHITESPACE,

        /**
         * \V
         */
        NON_VERTICAL_WHITESPACE,

        /**
         * \w
         */
        WORD,

        /**
         * \W
         */
        NON_WORD,

    }
}
