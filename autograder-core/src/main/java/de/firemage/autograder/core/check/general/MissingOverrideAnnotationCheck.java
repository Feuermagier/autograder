package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.MissingOverrideRule;

public class MissingOverrideAnnotationCheck extends PMDCheck {
    private static final String DESCRIPTION = "Missing @Override";

    public MissingOverrideAnnotationCheck() {
        super(DESCRIPTION, "Missing @Override", new MissingOverrideRule());
    }
}
