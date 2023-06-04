package de.firemage.autograder.core;

import fluent.bundle.FluentBundle;

@FunctionalInterface
public interface Translatable {
    String format(FluentBundle bundle);
}
