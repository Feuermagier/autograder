package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class DefaultPackageCheck extends PMDCheck {
    private static final String DESCRIPTION = "The default package should not be used";

    public DefaultPackageCheck() {
        super(DESCRIPTION, createXPathRule("default package", "Do not use the default package",
                "/CompilationUnit[not(./PackageDeclaration)]/TypeDeclaration[1]"),
            ProblemType.DEFAULT_PACKAGE_USED);
    }
}
