package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.check.Check;

public class PlaceholderCheck implements Check {
    @Override
    public String getDescription() {
        return "<placeholder>";
    }

    @Override
    public String getLinter() {
        return "<Placeholder Linter>";
    }
}
