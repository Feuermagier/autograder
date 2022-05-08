package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.Check;

public class PlaceholderCheck implements Check {
    @Override
    public String getDescription() {
        return "<placeholder>";
    }
}
