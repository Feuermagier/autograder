package de.firemage.codelinter.core.check.naming;

import de.firemage.codelinter.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.codestyle.LinguisticNamingRule;

public class LinguisticNamingCheck extends PMDCheck {
    private static final String DESCRIPTION = "The code element has a confusing name. See https://pmd.github.io/latest/pmd_rules_java_codestyle.html#linguisticnaming";

    public LinguisticNamingCheck() {
        super(DESCRIPTION, new LinguisticNamingRule());
    }
}
