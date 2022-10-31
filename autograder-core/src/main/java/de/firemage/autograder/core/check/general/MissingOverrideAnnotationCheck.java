package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.MissingOverrideRule;

public class MissingOverrideAnnotationCheck extends PMDCheck {
    private static final String DESCRIPTION = "Missing @Override";

    public MissingOverrideAnnotationCheck() {
        super(DESCRIPTION, "Missing @Override", new MissingOverrideRule(), ProblemType.OVERRIDE_ANNOTATION_MISSING);
    }
}
