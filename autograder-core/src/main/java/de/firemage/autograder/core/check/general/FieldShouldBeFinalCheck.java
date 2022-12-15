package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;

import java.util.Map;
import java.util.Set;

public class FieldShouldBeFinalCheck extends IntegratedCheck {
    public FieldShouldBeFinalCheck() {
        super(new LocalizedMessage("field-final-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (field.isFinal()) {
                    return;
                }

                Set<CtConstructor<?>> parentConstructors;
                if (field.getDeclaringType() instanceof CtClass clazz) {
                    parentConstructors = clazz.getConstructors();
                } else {
                    parentConstructors = Set.of();
                }

                boolean hasWrite = staticAnalysis.getModel()
                    .filterChildren(c -> c instanceof CtExecutable<?> e && !parentConstructors.contains(e))
                    .filterChildren(e -> e.filterChildren(
                        c -> c instanceof CtFieldWrite<?> w && w.getVariable().equals(field.getReference())).first() !=
                        null).first() != null;
                if (!hasWrite) {
                    addLocalProblem(field,
                        new LocalizedMessage("field-final-exp", Map.of("name", field.getSimpleName())),
                        ProblemType.FIELD_SHOULD_BE_FINAL);
                }
            }
        });
    }
}
