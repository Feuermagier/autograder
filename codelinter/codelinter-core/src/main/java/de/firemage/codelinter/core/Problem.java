package de.firemage.codelinter.core;

import de.firemage.codelinter.core.check.Check;
import java.nio.file.Path;

public interface Problem {
    Check getCheck();

    String getExplanation();

    String getDisplayLocation();
}
