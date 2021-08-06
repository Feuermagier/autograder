package de.firemage.codelinter.core.pmd;

public record PMDRuleset(String filename) {
    private static final String PMD_DIRECTORY = "pmd/";

    public String path() {
        return PMD_DIRECTORY + this.filename;
    }
}
