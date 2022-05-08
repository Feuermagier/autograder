package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class ConstantsInInterfaceCheck extends PMDCheck {
    private static final String DESCRIPTION = "Shared constants should be placed in enums or final classes and not in interfaces";

    public ConstantsInInterfaceCheck() {
        super(DESCRIPTION, createXPathRule("constants in interface", "//ClassOrInterfaceDeclaration[@Interface= true()][$ignoreIfHasMethods= false() or not(.//MethodDeclaration)]//FieldDeclaration"));
    }
}
