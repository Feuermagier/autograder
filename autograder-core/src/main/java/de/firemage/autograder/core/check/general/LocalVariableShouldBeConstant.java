package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;

import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.LOCAL_VARIABLE_SHOULD_BE_CONSTANT})
public class LocalVariableShouldBeConstant extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtLocalVariable<?>>() {
            @Override
            public void process(CtLocalVariable<?> ctLocalVariable) {
                if (ctLocalVariable.isImplicit() || !ctLocalVariable.getPosition().isValidPosition()) return;

                Optional<CtExpression<?>> ctExpression = SpoonUtil.getEffectivelyFinalExpression(staticAnalysis, ctLocalVariable.getReference());

                ctExpression.ifPresent(value -> {
                    if (value instanceof CtLiteral<?>) {
                        addLocalProblem(
                                ctLocalVariable,
                            new LocalizedMessage("local-variable-should-be-constant"),
                            ProblemType.LOCAL_VARIABLE_SHOULD_BE_CONSTANT
                        );
                    }
                });
            }
        });
    }
}
