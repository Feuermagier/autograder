package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

public class UtilityClassCheck extends IntegratedCheck {

    public UtilityClassCheck() {
        super("Utility classes must be final and must have a single no-args private constructor.");
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> clazz) {
                if (clazz.isClass() && !clazz.getMethods().isEmpty() && clazz.getMethods().stream().allMatch(CtMethod::isStatic)) {
                    if (!clazz.isFinal()) {
                        addLocalProblem(clazz, "Utility class is not final", ProblemType.UTILITY_CLASS_NOT_FINAL);
                    }
                    
                    if (clazz.getConstructors().stream().allMatch(CtConstructor::isImplicit)) {
                        addLocalProblem(clazz, "Utility classes must have a private no-arg constructor", ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR);
                    } else {
                        clazz.getConstructors().stream()
                            .filter(c -> !c.isImplicit() && !c.isPrivate() || !c.getParameters().isEmpty())
                            .forEach(
                                c -> addLocalProblem(c, "Utility classes must only have a single private no-arg constructor", ProblemType.UTILITY_CLASS_INVALID_CONSTRUCTOR));

                    }

                    clazz.getFields().stream()
                        .filter(f -> !f.isFinal())
                        .forEach(f -> addLocalProblem(f, "Utility classes must only have final fields", ProblemType.UTILITY_CLASS_MUTABLE_FIELD));

                    // TODO add mutable access to fields, e.g. Collection::add
                }
            }
        });
    }
}
