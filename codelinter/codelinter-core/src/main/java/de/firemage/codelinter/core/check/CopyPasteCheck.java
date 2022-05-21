package de.firemage.codelinter.core.check;

import lombok.Getter;

public class CopyPasteCheck implements Check {
    @Getter
    private final int tokenCount;

    public CopyPasteCheck(int tokenCount) {
        this.tokenCount = tokenCount;
    }


    @Override
    public String getDescription() {
        return """
                Duplicated code.
                """;
    }

    @Override
    public String getLinter() {
        return "CPD";
    }
}
