package de.firemage.codelinter.core;

import java.nio.file.Path;

public interface Problem {
    Check getCheck();

    String getExplanation();

    String getDisplayLocation(Path root);

    String displayAsString(Path root);
}
