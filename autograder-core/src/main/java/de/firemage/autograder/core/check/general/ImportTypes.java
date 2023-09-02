package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.Map;
import java.util.Optional;


@ExecutableCheck(reportedProblems = { ProblemType.IMPORT_TYPES })
public class ImportTypes extends IntegratedCheck {
    private static Optional<CtElement> findParentSourcePosition(CtElement ctElement) {
        CtElement currentElement = ctElement;

        while (currentElement.isParentInitialized() && !currentElement.getPosition().isValidPosition()) {
            try {
                currentElement = currentElement.getParent();
            } catch (ParentNotInitializedException e) {
                return Optional.empty();
            }
        }

        if (!currentElement.isParentInitialized() || !currentElement.getPosition().isValidPosition()) {
            return Optional.empty();
        }

        return Optional.of(currentElement);
    }

    private void reportProblem(SourcePosition sourcePosition, CtTypeReference<?> ctTypeReference) {
        addLocalProblem(
            CodePosition.fromSourcePosition(sourcePosition, ctTypeReference, this.getRoot()),
            new LocalizedMessage(
                "import-types",
                Map.of("type", ctTypeReference.prettyprint())
            ),
            ProblemType.IMPORT_TYPES
        );
    }

    private static boolean isSimplyQualified(CtTypeReference<?> ctTypeReference) {
        return ctTypeReference.isImplicit()
            || ctTypeReference.isSimplyQualified()
            || ctTypeReference.isPrimitive()
            || ctTypeReference instanceof CtTypeParameterReference;
    }

    private static boolean isQualified(CtTypeReference<?> ctTypeReference) {
        if (ctTypeReference instanceof CtArrayTypeReference<?> ctArrayTypeReference) {
            return isQualified(ctArrayTypeReference.getArrayType());
        }

        CtTypeReference<?> type = ctTypeReference;
        while (type.getDeclaringType() != null) {
            type = type.getDeclaringType();
        }

        return !isSimplyQualified(type) && (type.getPosition().isValidPosition() || type.getParent(CtArrayTypeReference.class) != null);
    }

    private void checkCtTypeReference(CtTypeReference<?> ctTypeReference) {
        boolean isQualified = isQualified(ctTypeReference);
        if (!isQualified) {
            for (CtTypeReference<?> typeReference : ctTypeReference.getActualTypeArguments()) {
                if (isQualified(typeReference)) {
                    isQualified = true;
                    break;
                }
            }
        }

        if (isQualified) {
            this.reportProblem(findParentSourcePosition(ctTypeReference).orElseThrow().getPosition(), ctTypeReference);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtField(CtField<T> ctVariable) {
                if (!ctVariable.isImplicit()) {
                    checkCtTypeReference(ctVariable.getType());
                }

                super.visitCtField(ctVariable);
            }

            @Override
            public <T> void visitCtLocalVariable(CtLocalVariable<T> ctVariable) {
                if (!ctVariable.isImplicit() && !ctVariable.isInferred()) {
                    checkCtTypeReference(ctVariable.getType());
                }

                super.visitCtLocalVariable(ctVariable);
            }

            @Override
            public <T> void visitCtParameter(CtParameter<T> ctVariable) {
                if (!ctVariable.isImplicit()) {
                    checkCtTypeReference(ctVariable.getType());
                }

                super.visitCtParameter(ctVariable);
            }
        });
    }
}
