package de.firemage.codelinter.core.check;

import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import de.firemage.codelinter.event.GetFieldEvent;
import de.firemage.codelinter.event.MethodEvent;
import de.firemage.codelinter.event.PutFieldEvent;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

public class FieldShouldBeLocalCheck extends IntegratedCheck {
    private static final String DESCRIPTION =
        "Fields should be converted to locals if they are always overwritten before being read.";

    public FieldShouldBeLocalCheck() {
        super(DESCRIPTION);
    }

    private static String formatExplanation(CtField<?> field) {
        return String.format(
            "Field '%s' of class '%s' should be converted to a local variable as every method overwrites it before reading it",
            field.getSimpleName(), field.getDeclaringType().getQualifiedName());
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
                    addLocalProblem(field, formatExplanation(field));
                }
            }
        });
    }
}
