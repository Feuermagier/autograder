package de.firemage.autograder.core.integrated;

import spoon.reflect.code.BinaryOperatorKind;

public final class PrintUtil {
    private PrintUtil() {
        
    }

    public static String printOperator(BinaryOperatorKind op) {
        return switch (op) {
            case OR -> "||";
            case AND -> "&&";
            case BITOR -> "|";
            case BITXOR -> "^";
            case BITAND -> "&";
            case EQ -> "==";
            case NE -> "!=";
            case LT -> "<";
            case GT -> ">";
            case LE -> "<=";
            case GE -> ">=";
            case SL -> "<<";
            case SR -> ">>";
            case USR -> ">>>";
            case PLUS -> "+";
            case MINUS -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case MOD -> "%";
            case INSTANCEOF -> "instanceof";
        };
    }
}
