package de.firemage.autograder.core.visualize.dot;

import java.util.Map;
import java.util.stream.Collectors;

public final class DotUtil {
    private DotUtil() {
    }

    public static String formatAttributes(String cssClass, Map<String, String> attributes) {
        String result = attributes.entrySet()
            .stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(" "));
        if (cssClass != null) {
            result += " class=" + cssClass;
        }
        return "[" + result + "]";
    }
}
