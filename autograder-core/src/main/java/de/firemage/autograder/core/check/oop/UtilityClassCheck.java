package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;

@ExecutableCheck(reportedProblems = {
    ProblemType.UTILITY_CLASS_NOT_FINAL,
    ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR,
})
public class UtilityClassCheck extends IntegratedCheck {
    public static boolean isUtilityClass(CtClass<?> ctClass) {
        return
            // it must obviously be a class
            ctClass.isClass()
                // ignore anonymous classes
                && !ctClass.isAnonymous()
                // should have at least one method
                && !ctClass.getMethods().isEmpty()
                // all methods should be static
                && ctClass.getMethods().stream().allMatch(CtMethod::isStatic)
                // all fields should be static
                && ctClass.getFields().stream().allMatch(CtModifiable::isStatic)
                // the class should not extend anything
                && ctClass.getSuperclass() == null
                // the class should not implement anything
                && ctClass.getSuperInterfaces().isEmpty()
                // the class itself should not be an inner class
                && !SpoonUtil.isInnerClass(ctClass);
    }

    private void checkCtClassConstructor(CtClass<?> ctClass, ProblemType problemType) {
        // check if there is no constructor, only the implicit default one:
        if (ctClass.getConstructors().stream().allMatch(CtConstructor::isImplicit)) {
            addLocalProblem(
                    ctClass,
                    new LocalizedMessage("utility-exp-constructor"),
                    problemType
            );
            return;
        }

        for (CtConstructor<?> ctConstructor : ctClass.getConstructors()) {
            if (ctConstructor.isImplicit() || ctConstructor.isPrivate() || !ctConstructor.getParameters().isEmpty()) {
                continue;
            }

            // if there is a non-private constructor, lint it:
            addLocalProblem(
                    ctConstructor,
                    new LocalizedMessage("utility-exp-constructor"),
                    problemType
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctClass) {
                // ignore everything that is not a utility class
                if (!isUtilityClass(ctClass)) {
                    return;
                }

                // evaluate abstract utility classes separately, so they can be disabled in the config
                if (ctClass.isAbstract()) {
                    checkCtClassConstructor(ctClass, ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR);

                    return;
                }

                // a utility class should be final
                if (!ctClass.isFinal()) {
                    addLocalProblem(ctClass, new LocalizedMessage("utility-exp-final"),
                        ProblemType.UTILITY_CLASS_NOT_FINAL
                    );
                }

                checkCtClassConstructor(ctClass, ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR);
            }
        });
    }
}
