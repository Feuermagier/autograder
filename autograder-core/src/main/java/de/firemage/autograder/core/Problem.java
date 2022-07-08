package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;

public interface Problem {
    Check getCheck();

    String getExplanation();

    String getDisplayLocation();
}
