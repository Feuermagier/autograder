package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;
import net.sourceforge.pmd.lang.java.rule.bestpractices.PrimitiveWrapperInstantiationRule;

public class WrapperInstantiationCheck extends PMDCheck {
    public WrapperInstantiationCheck() {
        super(new LocalizedMessage("wrapper-instantiation-desc"),
            new LocalizedMessage("wrapper-instantiation-exp"),
            new PrimitiveWrapperInstantiationRule(), 
            ProblemType.PRIMITIVE_WRAPPER_INSTANTIATION);
    }
}
