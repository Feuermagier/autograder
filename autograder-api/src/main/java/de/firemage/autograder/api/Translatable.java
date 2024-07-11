package de.firemage.autograder.api;

import fluent.bundle.FluentBundle;

@FunctionalInterface
public interface Translatable {
    String format(FluentBundle bundle);
}
