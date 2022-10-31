package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.MissingOverrideRule;

public class MissingOverrideAnnotationCheck extends PMDCheck {
    public MissingOverrideAnnotationCheck() {
        super(new LocalizedMessage("missing-override-desc"), new LocalizedMessage("missing-override-exp"),
            new MissingOverrideRule(), ProblemType.OVERRIDE_ANNOTATION_MISSING);
    }
}
