package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

@ExecutableCheck(reportedProblems = {
    ProblemType.UTILITY_CLASS_NOT_FINAL,
    ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR
})
public class UtilityClassCheck extends IntegratedCheck {

    public UtilityClassCheck() {
        super(new LocalizedMessage("utility-desc"));
    }

    public static boolean isUtilityClass(StaticAnalysis staticAnalysis, CtClass<?> ctClass) {
        return
            // it must obviously be a class
            ctClass.isClass()
                // ignore anonymous classes
                && !ctClass.isAnonymous()
                // should have at least one method
                && !ctClass.getMethods().isEmpty()
                // all methods should be static
                && ctClass.getMethods().stream().allMatch(CtMethod::isStatic)
                // all fields should be static and effectively final (no assignments)
                && ctClass.getFields().stream().allMatch(
                    ctField -> ctField.isStatic() && SpoonUtil.isEffectivelyFinal(staticAnalysis, ctField.getReference())
                )
                // the class should not be abstract
                && !ctClass.isAbstract()
                // the class should not extend anything
                && ctClass.getSuperclass() == null
                // the class should not implement anything
                && ctClass.getSuperInterfaces().isEmpty()
                // the class should not have any inner classes
                && ctClass.getNestedTypes().isEmpty()
                // the class itself should not be an inner class
                && !SpoonUtil.isInnerClass(ctClass);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> clazz) {
                // ignore everything that is not a utility class
                if (!isUtilityClass(staticAnalysis, clazz)) {
                    return;
                }

                if (!clazz.isFinal()) {
                    addLocalProblem(clazz, new LocalizedMessage("utility-exp-final"),
                        ProblemType.UTILITY_CLASS_NOT_FINAL
                    );
                }

                if (clazz.getConstructors().stream().allMatch(CtConstructor::isImplicit)) {
                    addLocalProblem(clazz, new LocalizedMessage("utility-exp-constructor"),
                        ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR
                    );
                } else {
                    clazz.getConstructors().stream()
                         .filter(c -> !c.isImplicit() && !c.isPrivate() || !c.getParameters().isEmpty())
                         .forEach(
                             c -> addLocalProblem(c, new LocalizedMessage("utility-exp-constructor"),
                                 ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR
                             ));

                }

                //                    clazz.getFields().stream()
                //                        .filter(f -> !f.isFinal())
                //                        .forEach(f -> addLocalProblem(f, new LocalizedMessage
                //                        ("utility-exp-field"),
                //                            ProblemType.UTILITY_CLASS_MUTABLE_FIELD));

                // TODO add mutable access to fields, e.g. Collection::add
            }
        });
    }
}
