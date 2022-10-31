package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class ConstantsInInterfaceCheck extends PMDCheck {
    private static final String QUERY = "//ClassOrInterfaceDeclaration[@Interface= true()]//FieldDeclaration";
    private static final String METHOD_IGNORING_QUERY =
        "//ClassOrInterfaceDeclaration[@Interface= true()][not(.//MethodDeclaration)]//FieldDeclaration";

    public ConstantsInInterfaceCheck() {
        this(false);
    }

    public ConstantsInInterfaceCheck(boolean ignoreIfHasMethods) {
        super(new LocalizedMessage("constants-interfaces-desc"),
            createXPathRule("constants in interface", "constants-interfaces-exp",
                ignoreIfHasMethods ? METHOD_IGNORING_QUERY : QUERY),
            ProblemType.CONSTANT_IN_INTERFACE);
    }
}
