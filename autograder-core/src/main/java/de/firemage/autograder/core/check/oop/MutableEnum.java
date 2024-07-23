package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.VariableUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

@ExecutableCheck(reportedProblems = { ProblemType.MUTABLE_ENUM })
public class MutableEnum extends IntegratedCheck {
    /**
     * Tries to detect if the provided type is mutable. It is very difficult to detect every possible
     * case of mutability, therefore this method might return false negatives.
     *
     * @param ctType the type to check
     * @return true if it is mutable, false if mutability cannot be determined
     */
    private static boolean isMutable(CtType<?> ctType) {
        for (CtField<?> ctField : ctType.getFields()) {
            if (!VariableUtil.isEffectivelyFinal(ctField)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtEnum<?>>() {
            @Override
            public void process(CtEnum<?> ctEnum) {
                if (isMutable(ctEnum)) {
                    addLocalProblem(
                        ctEnum,
                        new LocalizedMessage("mutable-enum"),
                        ProblemType.MUTABLE_ENUM
                    );
                }
            }
        });
    }
}
