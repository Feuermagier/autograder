package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.STATIC_FIELD_SHOULD_BE_INSTANCE})
public class StaticFieldShouldBeInstanceCheck extends IntegratedCheck {
    public StaticFieldShouldBeInstanceCheck() {
        super(new LocalizedMessage("static-field-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (!field.isStatic() || field.isFinal()) {
                    return;
                }

                if (!SpoonUtil.isEffectivelyFinal(staticAnalysis, field.getReference())) {
                    addLocalProblem(field,
                        new LocalizedMessage("static-field-exp", Map.of("name", field.getSimpleName())),
                        ProblemType.STATIC_FIELD_SHOULD_BE_INSTANCE);
                }
            }
        });
    }
}
