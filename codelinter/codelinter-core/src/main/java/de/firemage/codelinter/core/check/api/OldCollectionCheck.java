package de.firemage.codelinter.core.check.api;

import de.firemage.codelinter.core.pmd.PMDCheck;
import java.util.List;

public class OldCollectionCheck extends PMDCheck {
    public static final String DESCRIPTION = "Don't use Java's old collection types (Vector -> List, Stack -> Deque, Hashtable -> Map)";

    public OldCollectionCheck() {
        super(DESCRIPTION, List.of(
                createXPathRule("hashtable usage", "//Type/ReferenceType/ClassOrInterfaceType[@Image='Hashtable']"),
                createXPathRule("vector usage", "//Type/ReferenceType/ClassOrInterfaceType[@Image='Vector']"),
                createXPathRule("stack usage", "//Type/ReferenceType/ClassOrInterfaceType[@Image='Stack']")
        ));
    }
}
