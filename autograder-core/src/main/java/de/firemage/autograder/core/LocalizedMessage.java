package de.firemage.autograder.core;

import de.firemage.autograder.api.Translatable;
import fluent.bundle.FluentBundle;

import java.util.Map;

public record LocalizedMessage(String key, Map<String, ?> parameters) implements Translatable {
    public LocalizedMessage(String key) {
        this(key, Map.of());
    }

    @Override
    public String format(FluentBundle bundle) {
        return bundle.format(this.key, this.parameters);
    }
}
