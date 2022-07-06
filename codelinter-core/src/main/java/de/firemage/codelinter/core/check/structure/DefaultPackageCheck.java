package de.firemage.codelinter.core.check.structure;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class DefaultPackageCheck extends PMDCheck {
    private static final String DESCRIPTION = "The default package should not be used";

    public DefaultPackageCheck() {
        super(DESCRIPTION, createXPathRule("default package", "Do not use the default package", "/CompilationUnit[not(./PackageDeclaration)]/TypeDeclaration[1]"));
    }
}
