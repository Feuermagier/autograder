package de.firemage.autograder.core;

import fluent.bundle.FluentBundle;

import java.util.Map;

public record LocalizedMessage(String key, Map<String, ?>parameters) {
    public LocalizedMessage(String key) {
        this(key, Map.of());
    }
    
    public String format(FluentBundle bundle) {
        return bundle.format(this.key, this.parameters);
    }
}
