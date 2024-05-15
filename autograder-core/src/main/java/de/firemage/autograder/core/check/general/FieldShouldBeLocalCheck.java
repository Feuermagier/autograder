package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.event.GetFieldEvent;
import de.firemage.autograder.event.MethodEvent;
import de.firemage.autograder.event.PutFieldEvent;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.INSTANCE_FIELD_CAN_BE_LOCAL})
public class FieldShouldBeLocalCheck extends IntegratedCheck {
    private static LocalizedMessage formatExplanation(CtField<?> field) {
        return new LocalizedMessage("field-local-exp", Map.of(
            "field", field.getSimpleName(),
            "class", field.getDeclaringType().getQualifiedName()
        ));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (!field.isPrivate() || field.isFinal()) {
                    return;
                }

                boolean readBeforeWriteFound = false;
                boolean writeFound = false;
                outer:
                for (CtMethod<?> method : field.getDeclaringType().getMethods()) {
                    for (MethodEvent event : (Iterable<? extends MethodEvent>) dynamicAnalysis.findEventsForMethod(
                        method)::iterator) {
                        if (event instanceof GetFieldEvent e && e.getField().equals(field.getSimpleName())) {
                            readBeforeWriteFound = true;
                            break outer;
                        } else if (event instanceof PutFieldEvent e && e.getField().equals(field.getSimpleName())) {
                            writeFound = true;
                            break;
                        }
                    }
                }
                if (!readBeforeWriteFound && writeFound) {
                    addLocalProblem(field, formatExplanation(field), ProblemType.INSTANCE_FIELD_CAN_BE_LOCAL);
                }
            }
        });
    }
}
