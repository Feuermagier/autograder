package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.Translatable;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;

import java.util.function.Predicate;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_DEFAULT_CONSTRUCTOR})
public class RedundantConstructorCheck extends IntegratedCheck {
    private static final Translatable MESSAGE = new LocalizedMessage("implicit-constructor-exp");

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> element) {
                var ctors = element.getConstructors();
                if (ctors.size() != 1) return;

                var ctor = ctors.iterator().next();
                var isRedundant = !ctor.isImplicit()
                    && ctor.getParameters().isEmpty()
                    && hasDefaultVisibility(element, ctor)
                    && isEmpty(ctor.getBody())
                    && ctor.getThrownTypes().isEmpty();
                if (isRedundant) {
                    addLocalProblem(ctor, MESSAGE, ProblemType.REDUNDANT_DEFAULT_CONSTRUCTOR);
                }
            }
        });
    }

    private boolean hasDefaultVisibility(CtClass<?> type, CtConstructor<?> ctor) {
        // enum constructors are always private
        return type.isEnum() || ctor.getVisibility() == type.getVisibility();
    }

    private boolean isEmpty(CtBlock<?> block) {
        return block
            .getStatements()
            .stream()
            .filter(Predicate.not(CtElement::isImplicit))
            // A constructor invocation is either this or super.
            // Because we know we are analyzing the body of a no-args constructor, it
            // cannot be a recursive this() call, but has to be a redundant super() call.
            .filter(statement -> !(statement instanceof CtInvocation<?> invocation
                && invocation.getExecutable().isConstructor()
                && invocation.getArguments().isEmpty()))
            .findAny()
            .isEmpty();
    }
}
