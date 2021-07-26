package de.firemage.codelinter.linter.pmd;

public record PMDRuleset(String filename) {
    private static final String PMD_DIRECTORY = "pmd/";

    public String path() {
        return PMD_DIRECTORY + this.filename;
    }
}
