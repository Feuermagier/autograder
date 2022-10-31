package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.LooseCouplingRule;

public class ConcreteCollectionCheck extends PMDCheck {
    private static final String DESCRIPTION =
        "Use the parent interface instead of a concrete collection class (e.g. List instead of ArrayList)";

    public ConcreteCollectionCheck() {
        // Checks for fields, parameters and return types
        super(DESCRIPTION, DESCRIPTION, new LooseCouplingRule(),
            ProblemType.CONCRETE_COLLECTION_AS_FIELD_OR_RETURN_VALUE);
    }
}
