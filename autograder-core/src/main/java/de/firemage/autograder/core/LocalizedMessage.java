package de.firemage.autograder.core;

import de.firemage.autograder.api.Translatable;
import fluent.bundle.FluentBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record LocalizedMessage(String key, Map<String, ?> parameters) implements Translatable {
    private static final Logger logger = LoggerFactory.getLogger(LocalizedMessage.class);

    public LocalizedMessage(String key) {
        this(key, Map.of());
    }

    @Override
    public Optional<String> tryFormat(FluentBundle bundle) {
        var pattern = bundle.getMessagePattern(this.key);
        if (pattern.isEmpty()) {
            return Optional.empty();
        }

        List<Exception> errors = new ArrayList<>(1);
        var output = bundle.formatPattern(pattern.get(), this.parameters, errors);
        if (!errors.isEmpty()) {
            // To stay consistent with Fluent's FluentBundle#format(String key) method, we do not throw an exception here
            logger.error("Failed to format message '{}': {}", this.key, errors);
        }

        return Optional.of(output);
    }
}
