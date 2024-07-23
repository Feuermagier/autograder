package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.VariableUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;

import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.STATIC_FIELD_SHOULD_BE_INSTANCE })
public class StaticFieldShouldBeInstanceCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> ctField) {
                if (ctField.isImplicit() || !ctField.getPosition().isValidPosition() || !ctField.isStatic() || ctField.isFinal()) {
                    return;
                }

                // given the code:
                //
                // class Foo { static int counter = 0; Foo() { counter++; } } the field counter **must** be static

                // to keep the code simple, we ignore all fields that are a number:
                if (ctField.getType() != null && TypeUtil.isTypeEqualTo(ctField.getType().unbox(), int.class)) {
                    return;
                }

                // the field is not marked as final, so values can be assigned to it.
                // if the field is assigned multiple times, it should not be static
                if (!VariableUtil.isEffectivelyFinal(ctField)) {
                    addLocalProblem(
                        ctField,
                        new LocalizedMessage(
                            "static-field-should-be-instance",
                            Map.of("name", ctField.getSimpleName())
                        ),
                        ProblemType.STATIC_FIELD_SHOULD_BE_INSTANCE
                    );
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(4);
    }
}
