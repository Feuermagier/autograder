package de.firemage.autograder.core.integrated;

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
            if (!Character.isAlphabetic(c) || !Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
