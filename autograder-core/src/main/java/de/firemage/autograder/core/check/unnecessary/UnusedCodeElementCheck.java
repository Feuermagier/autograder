package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.reference.CtReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.DirectReferenceFilter;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.UNUSED_CODE_ELEMENT, ProblemType.UNUSED_CODE_ELEMENT_PRIVATE })
public class UnusedCodeElementCheck extends IntegratedCheck {
    private void checkUnused(CtNamedElement ctElement, CtReference ctReference) {
        if (ctElement.isImplicit() || !ctElement.getPosition().isValidPosition()) {
            return;
        }

        boolean isUnused = ctElement.getFactory()
            .getModel()
            .getElements(new DirectReferenceFilter<>(ctReference))
            .isEmpty();


        ProblemType problemType = ProblemType.UNUSED_CODE_ELEMENT;
        if (ctElement instanceof CtModifiable ctModifiable && ctModifiable.isPrivate()) {
            problemType = ProblemType.UNUSED_CODE_ELEMENT_PRIVATE;
        }

        if (isUnused) {
            addLocalProblem(
                ctElement,
                new LocalizedMessage("unused-element", Map.of("name", ctElement.getSimpleName())),
                problemType
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtLocalVariable(CtLocalVariable<T> ctLocalVariable) {
                checkUnused(ctLocalVariable, ctLocalVariable.getReference());
                super.visitCtLocalVariable(ctLocalVariable);
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                if (SpoonUtil.isOverriddenMethod(ctMethod) || SpoonUtil.isMainMethod(ctMethod)) {
                    super.visitCtMethod(ctMethod);
                    return;
                }

                checkUnused(ctMethod, ctMethod.getReference());
                super.visitCtMethod(ctMethod);
            }

            @Override
            public <T> void visitCtParameter(CtParameter<T> ctParameter) {
                if (SpoonUtil.isInOverriddenMethod(ctParameter) || SpoonUtil.isInMainMethod(ctParameter)) {
                    super.visitCtParameter(ctParameter);
                    return;
                }

                checkUnused(ctParameter, ctParameter.getReference());

                super.visitCtParameter(ctParameter);
            }

            @Override
            public void visitCtTypeParameter(CtTypeParameter ctTypeParameter) {
                checkUnused(ctTypeParameter, ctTypeParameter.getReference());
                super.visitCtTypeParameter(ctTypeParameter);
            }

            @Override
            public <T> void visitCtField(CtField<T> ctField) {
                checkUnused(ctField, ctField.getReference());
                super.visitCtField(ctField);
            }
        });
    }
}
