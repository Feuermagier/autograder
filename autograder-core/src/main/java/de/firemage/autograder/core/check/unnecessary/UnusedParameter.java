package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

@ExecutableCheck(reportedProblems = { ProblemType.UNUSED_CODE_ELEMENT })

public class UnusedParameter extends IntegratedCheck {
    public UnusedParameter() {
        super(new LocalizedMessage("unused-element-desc"));
    }

    private void checkCtExecutable(CtExecutable<?> ctExecutable) {
        for (CtParameter<?> parameter : ctExecutable.getParameters()) {
            boolean isUnused = ctExecutable
                    .filterChildren(ctElement -> ctElement instanceof CtVariableAccess<?> ctVariableAccess
                            && ctVariableAccess.getVariable().equals(parameter.getReference()))
                    .first() == null;

            if (isUnused) {
                addLocalProblem(
                        parameter,
                        new LocalizedMessage("unused-element-exp"),
                        ProblemType.UNUSED_CODE_ELEMENT
                );
            }
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtExecutable<?>>() {
            @Override
            public void process(CtExecutable<?> ctExecutable) {
                if (ctExecutable.isImplicit() || !ctExecutable.getPosition().isValidPosition()) return;

                if (ctExecutable instanceof CtMethod<?> ctMethod) {
                    if (SpoonUtil.isOverriddenMethod(ctMethod)) {
                        return;
                    }

                    checkCtExecutable(ctExecutable);
                } else if (ctExecutable instanceof CtConstructor<?>) {
                    checkCtExecutable(ctExecutable);
                }
            }
        });
    }
}
