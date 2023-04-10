package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;

@ExecutableCheck(reportedProblems = { ProblemType.AVOID_INNER_CLASSES })
public class AvoidInnerClasses extends IntegratedCheck {
    public AvoidInnerClasses() {
        super(new LocalizedMessage("avoid-inner-classes"));
    }

    private void checkCtType(CtType<?> ctType) {
        // only lint non-private static inner classes
        if (SpoonUtil.isInnerClass(ctType) && !ctType.isPrivate() && (ctType.isStatic() || ctType.isInterface() || ctType.isEnum() || ctType.isLocalType())) {
            this.addLocalProblem(
                ctType,
                new LocalizedMessage("avoid-inner-classes"),
                ProblemType.AVOID_INNER_CLASSES
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> ctType) {
                checkCtType(ctType);
            }
        });
    }
}
