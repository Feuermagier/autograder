package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.pmd.PMDCheck;

@ExecutableCheck(reportedProblems = {ProblemType.DEFAULT_PACKAGE_USED})
public class DefaultPackageCheck extends PMDCheck {
    public DefaultPackageCheck() {
        super(new LocalizedMessage("default-package-desc"),
            createXPathRule("default package", "default-package-exp",
                "/CompilationUnit[not(./PackageDeclaration)]/TypeDeclaration[1]"),
            ProblemType.DEFAULT_PACKAGE_USED);
    }
}
