package de.firemage.autograder.core;

import de.firemage.autograder.core.check.Check;

public interface Problem {
    Check getCheck();

    LocalizedMessage getExplanation();

    String getDisplayLocation();
    
    ProblemType getProblemType();
}
