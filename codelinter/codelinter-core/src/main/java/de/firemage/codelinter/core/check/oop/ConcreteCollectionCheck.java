package de.firemage.codelinter.core.check.oop;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.LooseCouplingRule;

public class ConcreteCollectionCheck extends PMDCheck {
    private static final String DESCRIPTION = "Use the parent interface instead of a concrete collection class (e.g. List instead of ArrayList)";

    public ConcreteCollectionCheck() {
        // Checks for fields, parameters and return types
        super(DESCRIPTION, new LooseCouplingRule());
    }
}
