package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.LooseCouplingRule;

public class ConcreteCollectionCheck extends PMDCheck {
    public ConcreteCollectionCheck() {
        // Checks for fields, parameters and return types
        super(new LocalizedMessage("concrete-collection-desc"), new LocalizedMessage("concrete-collection-exp"),
            new LooseCouplingRule(), ProblemType.CONCRETE_COLLECTION_AS_FIELD_OR_RETURN_VALUE);
    }
}
