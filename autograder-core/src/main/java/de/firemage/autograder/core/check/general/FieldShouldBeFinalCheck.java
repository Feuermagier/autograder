package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.FIELD_SHOULD_BE_FINAL})
public class FieldShouldBeFinalCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> ctField) {
                if (ctField.isFinal()) {
                    return;
                }

                boolean hasWrite = staticAnalysis.getModel()
                        .filterChildren(ctElement -> ctElement instanceof CtFieldWrite<?> ctFieldWrite
                            && ctFieldWrite.getVariable().getDeclaration().equals(ctField)
                            && ctElement.getParent(CtConstructor.class) == null
                        )
                        .first() != null;

                boolean hasWriteInConstructor = staticAnalysis.getModel()
                        .filterChildren(ctElement -> ctElement instanceof CtFieldWrite<?> ctFieldWrite
                            && ctFieldWrite.getVariable().getDeclaration().equals(ctField)
                            && ctElement.getParent(CtConstructor.class) != null
                        )
                        .first() != null;

                boolean hasExplicitValue = ctField.getDefaultExpression() != null && !ctField.getDefaultExpression().isImplicit();

                if (!hasWrite && !(hasWriteInConstructor && hasExplicitValue)) {
                    addLocalProblem(
                        ctField,
                        new LocalizedMessage("field-final-exp", Map.of("name", ctField.getSimpleName())),
                        ProblemType.FIELD_SHOULD_BE_FINAL
                    );
                }
            }
        });
    }
}
