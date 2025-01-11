package de.firemage.autograder.core;

import de.firemage.autograder.api.AbstractTranslations;
import de.firemage.autograder.api.Translatable;
import fluent.bundle.FluentBundle;

import java.util.Optional;

public record LocalizedMessageForProblem(Translatable translatable, ProblemType problemType) implements Translatable {
    public LocalizedMessageForProblem {
        if (translatable instanceof LocalizedMessageForProblem) {
            throw new IllegalArgumentException("LocalizedMessageForProblem cannot be nested");
        }
    }

    @Override
    public String format(AbstractTranslations translations) {
        var conditionalBundle = translations.getConditionalTranslations(problemType);
        if (conditionalBundle != null) {
            var result = translatable.tryFormat(conditionalBundle);
            if (result.isPresent()) {
                return result.get();
            }
        }

        return translatable.format(translations.getMainTranslations());
    }

    @Override
    public Optional<String> tryFormat(FluentBundle bundle) {
        return translatable.tryFormat(bundle);
    }
}
