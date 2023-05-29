package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.IMPORT_TYPES })
public class ImportTypes extends IntegratedCheck {
    private static boolean isFullyQualifiedType(CtTypeReference<?> ctTypeReference) {
        // if this code breaks in the future, one might do this instead:
        // ctTypeReference.getOriginalSourceFragment().getSourceCode().startsWith(ctTypeReference.getQualifiedName())
        return !ctTypeReference.isSimplyQualified()
            && !ctTypeReference.isPrimitive()
            && !SpoonUtil.isInnerClass(ctTypeReference);
    }

    private void reportProblem(CtTypeReference<?> ctTypeReference) {
        reportProblem(ctTypeReference, ctTypeReference);
    }

    private void reportProblem(CtElement ctElement, CtTypeReference<?> ctTypeReference) {
        addLocalProblem(
            ctElement,
            new LocalizedMessage(
                "import-types",
                Map.of(
                    "type", ctTypeReference.getQualifiedName()
                )
            ),
            ProblemType.IMPORT_TYPES
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTypeReference<?>>() {
            @Override
            public void process(CtTypeReference<?> ctTypeReference) {
                // Special case arrays, because they do not have a valid source position.
                //
                // The source position is not valid, because one can write String a[] and String[] a and spoon
                // does not support multi-spans
                if (ctTypeReference instanceof CtArrayTypeReference<?> ctArrayTypeReference
                    // skip nested arrays (they are already covered, because the code is executed on the parent as well)
                    && ctTypeReference.getParent(CtArrayTypeReference.class) == null) {
                    CtTypeReference<?> arrayType = ctArrayTypeReference.getArrayType();
                    if (isFullyQualifiedType(arrayType)) {
                        reportProblem(ctTypeReference.getParent(), arrayType);
                    }

                    return;
                }

                if (ctTypeReference.isImplicit() || !ctTypeReference.getPosition().isValidPosition()) {
                    return;
                }

                if (isFullyQualifiedType(ctTypeReference)) {
                    reportProblem(ctTypeReference);
                }
            }
        });
    }
}
