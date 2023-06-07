package de.firemage.autograder.core.integrated;

import com.google.common.base.CaseFormat;

public final class IdentifierNameUtils {
    private IdentifierNameUtils() {

    }

    public static boolean isUpperSnakeCase(String identifier) {
        for (char c : identifier.toCharArray()) {
            if (!Character.isUpperCase(c) && c != '_' && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLowerCamelCase(String identifier) {
        if (!Character.isLowerCase(identifier.charAt(0))) {
            return false;
        }

        return isCamelCase(identifier);
    }

    public static boolean isUpperCamelCase(String identifier) {
        if (!Character.isUpperCase(identifier.charAt(0))) {
            return false;
        }

        return isCamelCase(identifier);
    }

    public static boolean isCamelCase(String identifier) {
        for (char c : identifier.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String toUpperSnakeCase(String identifier) {
        return getCaseFormat(identifier).converterTo(CaseFormat.UPPER_UNDERSCORE).convert(identifier);
    }

    public static String toLowerCamelCase(String identifier) {
        return getCaseFormat(identifier).converterTo(CaseFormat.LOWER_CAMEL).convert(identifier);
    }

    private static CaseFormat getCaseFormat(String identifier) {
        if (isLowerCamelCase(identifier)) {
            return CaseFormat.LOWER_CAMEL;
        }

        if (isUpperCamelCase(identifier)) {
            return CaseFormat.UPPER_CAMEL;
        }

        if (isUpperSnakeCase(identifier)) {
            return CaseFormat.UPPER_UNDERSCORE;
        }

        throw new IllegalArgumentException("Identifier '%s' is not in a (supported) valid format".formatted(identifier));
    }
}
