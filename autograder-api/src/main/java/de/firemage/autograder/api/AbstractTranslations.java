package de.firemage.autograder.api;

import fluent.bundle.FluentBundle;

public interface AbstractTranslations {
    FluentBundle getMainTranslations();
    FluentBundle getConditionalTranslations(AbstractProblemType problemType);
}
