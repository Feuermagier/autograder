package de.firemage.autograder.extra.integrated;

import com.google.common.base.CaseFormat;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Stream;

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
        // If the identifier is already in upper snake case like `DAMAGE`,
        // don't change it, which would otherwise result in `D_A_M_A_G_E`.
        if (isUpperSnakeCase(identifier)) {
            return identifier;
        }

        return getCaseFormat(identifier).converterTo(CaseFormat.UPPER_UNDERSCORE).convert(identifier);
    }

    public static String toLowerCamelCase(String identifier) {
        if (isLowerCamelCase(identifier)) {
            return identifier;
        }

        return getCaseFormat(identifier).converterTo(CaseFormat.LOWER_CAMEL).convert(identifier);
    }

    /**
     * Splits an identifier into its parts. For example "getFooBar" will be split into "get", "foo" and "bar".
     *
     * @param identifier the identifier to split up, must not be null and should follow a known naming convention
     * @return the parts of the identifier, every word is in lowercase
     */
    public static Stream<String> split(String identifier) {
        // should not return null, because identifier should not be null
        return Arrays.stream(toUpperSnakeCase(identifier).split("_"))
            .map(String::toLowerCase);
    }

    private static CaseFormat getCaseFormat(String identifier) {
        identifier = Normalizer.normalize(identifier, Normalizer.Form.NFC);

        if (isLowerCamelCase(identifier)) {
            return CaseFormat.LOWER_CAMEL;
        }

        if (isUpperCamelCase(identifier)) {
            return CaseFormat.UPPER_CAMEL;
        }

        if (isUpperSnakeCase(identifier)) {
            return CaseFormat.UPPER_UNDERSCORE;
        }

        if (isUpperSnakeCase(identifier.toUpperCase())) {
            return CaseFormat.LOWER_UNDERSCORE;
        }

        throw new IllegalArgumentException("Identifier '%s' is not in a (supported) naming convention".formatted(identifier));
    }
}
