package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.MissingOverrideRule;

public class MissingOverrideAnnotationCheck extends PMDCheck {
    private static final String DESCRIPTION = "Missing @Override";

    public MissingOverrideAnnotationCheck() {
        super(DESCRIPTION, "Missing @Override", new MissingOverrideRule());
    }
}
