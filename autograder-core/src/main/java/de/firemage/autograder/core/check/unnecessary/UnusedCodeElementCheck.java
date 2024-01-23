package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.visitor.CtScanner;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.UNUSED_CODE_ELEMENT, ProblemType.UNUSED_CODE_ELEMENT_PRIVATE })
public class UnusedCodeElementCheck extends IntegratedCheck {
    private void checkUnused(StaticAnalysis staticAnalysis, CtNamedElement ctElement) {
        if (ctElement.isImplicit() || !ctElement.getPosition().isValidPosition()) {
            return;
        }

        List<CtElement> references = SpoonUtil.findUses(ctElement);

        boolean isUnused;
        if (ctElement instanceof CtMethod<?> method) {
            // Methods are also unused if they are only called by themselves, i.e. they are recursive
            isUnused = references.stream().allMatch(r -> method.equals(r.getParent(CtMethod.class)));
        } else {
            isUnused = references.isEmpty();
        }


        ProblemType problemType = ProblemType.UNUSED_CODE_ELEMENT;
        if (ctElement instanceof CtModifiable ctModifiable && ctModifiable.isPrivate()) {
            problemType = ProblemType.UNUSED_CODE_ELEMENT_PRIVATE;
        }

        if (isUnused) {
            // do not report unused elements if there is no main method in the model and the element is accessible
            // (i.e. not private)
            if (ctElement instanceof CtParameter<?>
                && ctElement.getParent() instanceof CtTypeMember ctTypeMember
                && !ctTypeMember.getDeclaringType().isPrivate()
                // check if there is no main method in the model
                && !staticAnalysis.getCodeModel().hasMainMethod()) {
                return;
            }

            if (ctElement instanceof CtModifiable ctModifiable
                && !ctModifiable.isPrivate()
                && ctModifiable instanceof CtTypeMember ctTypeMember
                && !ctTypeMember.getDeclaringType().isPrivate()
                // check if there is no main method in the model
                && !staticAnalysis.getCodeModel().hasMainMethod()
            ) {
                return;
            }

            String name = ctElement.getSimpleName();

            // in spoon constructors are called <init>, which is not helpful
            if (ctElement instanceof CtConstructor<?> ctConstructor) {
                name = "%s()".formatted(ctConstructor.getDeclaringType().getSimpleName());
            }

            addLocalProblem(
                ctElement,
                new LocalizedMessage("unused-element", Map.of("name", name)),
                problemType
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtLocalVariable(CtLocalVariable<T> ctLocalVariable) {
                checkUnused(staticAnalysis, ctLocalVariable);
                super.visitCtLocalVariable(ctLocalVariable);
            }

            @Override
            public <T> void visitCtMethod(CtMethod<T> ctMethod) {
                if (SpoonUtil.isOverriddenMethod(ctMethod)
                    || SpoonUtil.isMainMethod(ctMethod)) {
                    super.visitCtMethod(ctMethod);
                    return;
                }

                checkUnused(staticAnalysis, ctMethod);
                super.visitCtMethod(ctMethod);
            }

            @Override
            public <T> void visitCtConstructor(CtConstructor<T> ctConstructor) {
                if (ctConstructor.isPrivate()) {
                    super.visitCtConstructor(ctConstructor);
                    return;
                }

                checkUnused(staticAnalysis, ctConstructor);
                super.visitCtConstructor(ctConstructor);
            }

            @Override
            public <T> void visitCtParameter(CtParameter<T> ctParameter) {
                if (SpoonUtil.isInOverriddenMethod(ctParameter)
                    || SpoonUtil.isInMainMethod(ctParameter)
                    || ctParameter.getParent() instanceof CtLambda<?>
                    || ctParameter.getParent(CtInterface.class) != null) {
                    super.visitCtParameter(ctParameter);
                    return;
                }

                checkUnused(staticAnalysis, ctParameter);

                super.visitCtParameter(ctParameter);
            }

            @Override
            public void visitCtTypeParameter(CtTypeParameter ctTypeParameter) {
                checkUnused(staticAnalysis, ctTypeParameter);
                super.visitCtTypeParameter(ctTypeParameter);
            }

            @Override
            public <T> void visitCtField(CtField<T> ctField) {
                if (ctField.getSimpleName().equals("serialVersionUID")) {
                    super.visitCtField(ctField);
                    return;
                }

                checkUnused(staticAnalysis, ctField);
                super.visitCtField(ctField);
            }

            // do not want to deal with false-positives for now
            /*
            @Override
            public <T> void visitCtClass(CtClass<T> ctType) {
                if (ctType.isAnonymous() || ctType.getMethods().stream().anyMatch(SpoonUtil::isMainMethod)) {
                    super.visitCtClass(ctType);
                    return;
                }

                checkUnused(staticAnalysis, ctType);
                super.visitCtClass(ctType);
            }

            @Override
            public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctEnum) {
                checkUnused(staticAnalysis, ctEnum);
                super.visitCtEnum(ctEnum);
            }*/
        });
    }
}
