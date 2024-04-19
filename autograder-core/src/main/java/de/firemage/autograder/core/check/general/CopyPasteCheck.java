package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.check.Check;

// TODO add executable check annotation and problem type
public class CopyPasteCheck implements Check {
    private final int tokenCount;

    public CopyPasteCheck(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    @Override
    public LocalizedMessage getLinter() {
        return new LocalizedMessage("linter-cpd");
    }

    public int getTokenCount() {
        return tokenCount;
    }
}
