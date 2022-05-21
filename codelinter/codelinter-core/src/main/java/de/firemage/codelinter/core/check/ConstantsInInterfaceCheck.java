package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class ConstantsInInterfaceCheck extends PMDCheck {
    private static final String DESCRIPTION = "Shared constants should be placed in enums or final classes and not in interfaces";
    private static final String QUERY = "//ClassOrInterfaceDeclaration[@Interface= true()]//FieldDeclaration";
    private static final String METHOD_IGNORING_QUERY = "//ClassOrInterfaceDeclaration[@Interface= true()][not(.//MethodDeclaration)]//FieldDeclaration";

    public ConstantsInInterfaceCheck(boolean ignoreIfHasMethods) {
        super(DESCRIPTION, createXPathRule("constants in interface", "Interfaces must not have fields", ignoreIfHasMethods ? METHOD_IGNORING_QUERY : QUERY));
    }
}
