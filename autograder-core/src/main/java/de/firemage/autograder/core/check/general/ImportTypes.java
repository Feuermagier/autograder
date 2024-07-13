package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;
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

    private static boolean isSimplyQualified(CtTypeReference<?> ctTypeReference) {
        return ctTypeReference.isImplicit()
            || ctTypeReference.isSimplyQualified()
            || ctTypeReference.isPrimitive()
            || ctTypeReference instanceof CtTypeParameterReference;
    }

    private static List<CtTypeReference<?>> findQualifiedTypes(CtTypeReference<?> ctTypeReference) {
        if (ctTypeReference instanceof CtArrayTypeReference<?> ctArrayTypeReference) {
            return findQualifiedTypes(ctArrayTypeReference.getArrayType());
        }

        // for nested types, we only want to check the outermost type
        CtTypeReference<?> type = ctTypeReference;
        while (type.getDeclaringType() != null) {
            type = type.getDeclaringType();
        }

        List<CtTypeReference<?>> result = new ArrayList<>();
        // The not simplified type check does not always work, especially when spoon is buggy.
        // See forEach test, where the type is not implicit, but its parent is.
        //
        // I can not think of a case where the parent is implicit and the child is not, so I added
        // this as a workaround to avoid making a spoon PR.
        if (!isSimplyQualified(type) && !type.getParent().isImplicit()) {
            result.add(type);
        }

        for (CtTypeReference<?> typeArgument : type.getActualTypeArguments()) {
            result.addAll(findQualifiedTypes(typeArgument));
        }

        return result;
    }

    private void checkCtTypeReference(CtTypeReference<?> ctTypeReference) {
        CodePosition codePosition = CodePosition.fromSourcePosition(
            findParentSourcePosition(ctTypeReference).orElseThrow().getPosition(),
            ctTypeReference,
            this.getRoot()
        );

        for (CtTypeReference<?> typeReference : findQualifiedTypes(ctTypeReference)) {
            this.addLocalProblem(
                codePosition,
                new LocalizedMessage(
                    "import-types",
                    Map.of("type", typeReference.getQualifiedName())
                ),
                ProblemType.IMPORT_TYPES
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
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
                // !isInferred to skip `var`
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
