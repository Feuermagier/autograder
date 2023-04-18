package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.check.oop.UtilityClassCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtType;

@ExecutableCheck(reportedProblems = { ProblemType.UNREACHABLE_CODE_PRIVATE_CONSTRUCTOR })
public class UnreachableCode extends IntegratedCheck {
    public UnreachableCode() {
        super(new LocalizedMessage("unreachable-code-private-constructor"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructor<?>>() {
            @Override
            public void process(CtConstructor<?> ctConstructor) {
                // skip non-private constructors and those which may have invalid positions
                if (!ctConstructor.isPrivate()
                        || ctConstructor.isImplicit()
                        || !ctConstructor.getPosition().isValidPosition()) {
                    return;
                }

                // The type in which the constructor is declared
                CtType<?> ctType = ctConstructor.getDeclaringType();

                // only check utility classes:
                if (!(ctType instanceof CtClass<?> ctClass) || !UtilityClassCheck.isUtilityClass(staticAnalysis, ctClass)) {
                    return;
                }

                // there may be some weird edge cases, where only a private constructor exists
                // that is used, but not returned by a factory method
                boolean hasFactoryMethod = ctClass.getMethods().stream()
                        .anyMatch(ctMethod -> ctMethod.isStatic() && ctMethod.getType().equals(ctType.getReference()));

                if (hasFactoryMethod) {
                    return;
                }

                if (!SpoonUtil.getEffectiveStatements(ctConstructor.getBody()).isEmpty()) {
                    addLocalProblem(
                        ctConstructor,
                        new LocalizedMessage("unreachable-code-private-constructor"),
                        ProblemType.UNREACHABLE_CODE_PRIVATE_CONSTRUCTOR
                    );
                }
            }
        });
    }
}
