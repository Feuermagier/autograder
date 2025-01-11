package de.firemage.autograder.api;

import fluent.bundle.FluentBundle;

import java.util.Optional;

@FunctionalInterface
public interface Translatable {
    /**
     *
     * @param bundle The bundle to format the message with
     * @return an empty optional if the message key is unknown.
     * Does not return an empty optional if the message key is known but the message cannot be formatted.
     */
    Optional<String> tryFormat(FluentBundle bundle);

    default String format(FluentBundle bundle) {
        return tryFormat(bundle).orElseThrow(() -> new IllegalStateException("Unknown message"));
    }

    default String format(AbstractTranslations translations) {
        return format(translations.getMainTranslations());
    }
}
