package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtBodyHolder;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ExecutableCheck(reportedProblems = {ProblemType.FOR_CAN_BE_FOREACH})
public class ForToForEachLoop extends IntegratedCheck {
    private static final Function<CtVariableAccess<?>, Optional<CtVariableAccess<?>>> LOOP_VARIABLE_ACCESS_STRING = ctVariableAccess -> {
        if (ctVariableAccess.getParent() instanceof CtInvocation<?> ctInvocation
                && MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), char.class, "charAt", int.class)
                && ctInvocation.getTarget() instanceof CtVariableAccess<?> variableAccess) {
            return Optional.of(variableAccess);
        }

        return Optional.empty();
    };

    private static final Function<CtVariableAccess<?>, Optional<CtVariableAccess<?>>> LOOP_VARIABLE_ACCESS_ARRAY = ctVariableAccess -> {
        if (ctVariableAccess.getParent() instanceof CtArrayRead<?> arrayAccess
                && arrayAccess.getTarget() instanceof CtVariableAccess<?> variableAccess) {
            return Optional.of(variableAccess);
        }

        return Optional.empty();
    };

    public static final Function<CtVariableAccess<?>, Optional<CtVariableAccess<?>>> LOOP_VARIABLE_ACCESS_LIST = ctVariableAccess -> {
        if (ctVariableAccess.getParent() instanceof CtInvocation<?> ctInvocation
                // && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), Object.class, "get", int.class)
                && ctInvocation.getExecutable().getSimpleName().equals("get")
                && ctInvocation.getTarget() instanceof CtVariableAccess<?> variableAccess
                && TypeUtil.isSubtypeOf(variableAccess.getType(), java.util.List.class)) {
            return Optional.of(variableAccess);
        }

        return Optional.empty();
    };

    public static Optional<CtVariable<?>> getForEachLoopVariable(
        CtBodyHolder ctBodyHolder,
        ForLoopRange forLoopRange,
        Function<? super CtVariableAccess<?>, Optional<CtVariableAccess<?>>> getPotentialLoopVariableAccess
    ) {
        CtVariable<?> loopVariable = forLoopRange.loopVariable().getDeclaration();

        // if a ForLoopRange exists, it is guaranteed that the variable is incremented by 1 for each step

        CtVariable<?> ctVariable = null;
        CtVariableAccess<?> expectedAccess = null;
        for (CtVariableAccess<?> ctVariableAccess : UsesFinder.variableUses(loopVariable).nestedIn(ctBodyHolder.getBody()).iterable()) {
            // We need to get the variable on which the access is performed (e.g. in a[i] we need to get a)
            CtVariableAccess<?> potentialLoopVariableAccess = getPotentialLoopVariableAccess.apply(ctVariableAccess)
                    .orElse(null);

            if (!(potentialLoopVariableAccess instanceof CtVariableRead<?>)) {
                return Optional.empty();
            }

            if (expectedAccess == null) {
                expectedAccess = potentialLoopVariableAccess;
            }

            if (!expectedAccess.equals(potentialLoopVariableAccess)) {
                return Optional.empty();
            }

            CtVariableReference<?> potentialVariable = potentialLoopVariableAccess.getVariable();

            if (potentialVariable.getDeclaration() == null) {
                return Optional.empty();
            }

            if (ctVariable == null) {
                ctVariable = potentialVariable.getDeclaration();
            }

            // check if the variable is the same for all accesses, otherwise it cannot be used in a for-each loop
            if (!ctVariable.equals(potentialVariable.getDeclaration())) {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(ctVariable);
    }

    // The condition of a for loop will look like
    // - i < array.length
    // - i < collection.size()
    // - i < string.length()
    //
    // based on the above, we can find the variable that should be iterated over
    public static Optional<CtVariable<?>> findIterable(ForLoopRange forLoopRange) {
        CtExpression<?> end = forLoopRange.end();

        // check if the end condition is array.length
        if (end instanceof CtFieldRead<?> ctFieldRead
                && ctFieldRead.getVariable().getSimpleName().equals("length")
                && ctFieldRead.getTarget() instanceof CtVariableAccess<?> target
                && target.getType().isArray()) {
            return Optional.ofNullable(target.getVariable().getDeclaration());
        }

        // check if the end condition is collection.size()
        if (end instanceof CtInvocation<?> ctInvocation
                && MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), int.class, "size")
                && ctInvocation.getTarget() instanceof CtVariableAccess<?> target
                && TypeUtil.isSubtypeOf(target.getType(), java.util.Collection.class)) {
            return Optional.ofNullable(target.getVariable().getDeclaration());
        }

        // check if the end condition is string.length()
        if (end instanceof CtInvocation<?> ctInvocation
                && MethodUtil.isSignatureEqualTo(ctInvocation.getExecutable(), int.class, "length")
                && ctInvocation.getTarget() instanceof CtVariableAccess<?> target
                && TypeUtil.isTypeEqualTo(target.getType(), java.lang.String.class)) {
            return Optional.ofNullable(target.getVariable().getDeclaration());
        }


        return Optional.empty();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtFor>() {
            @Override
            public void process(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    return;
                }

                ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);
                if (forLoopRange == null) {
                    return;
                }

                // for loop must start at index 0
                if (!(forLoopRange.start() instanceof CtLiteral<Integer> ctLiteral) || ctLiteral.getValue() != 0) {
                    return;
                }

                CtVariable<?> iterable = findIterable(forLoopRange).orElse(null);
                if (iterable == null) {
                    return;
                }

                CtTypeReference<?> elementType = ctFor.getFactory().createCtTypeReference(java.lang.Object.class);
                String iterableExpression = iterable.getSimpleName();

                Function<CtVariableAccess<?>, Optional<CtVariableAccess<?>>> getPotentialLoopVariableAccess;
                if (SpoonUtil.isString(iterable.getType())) {
                    getPotentialLoopVariableAccess = LOOP_VARIABLE_ACCESS_STRING;

                    iterableExpression = "%s.toCharArray()".formatted(iterableExpression);
                    elementType = ctFor.getFactory().createCtTypeReference(char.class);
                } else if (TypeUtil.isSubtypeOf(iterable.getType(), java.util.List.class)) {
                    getPotentialLoopVariableAccess = LOOP_VARIABLE_ACCESS_LIST;

                    // size != 1, if the list is a raw type: List list = new ArrayList();
                    if (iterable.getType().getActualTypeArguments().size() == 1) {
                        elementType = iterable.getType().getActualTypeArguments().get(0).unbox();
                    }
                } else if (iterable.getType() instanceof CtArrayTypeReference<?> arrayTypeReference) {
                    getPotentialLoopVariableAccess = LOOP_VARIABLE_ACCESS_ARRAY;

                    elementType = arrayTypeReference.getComponentType();
                } else {
                    // unknown iterable type
                    return;
                }

                CtVariable<?> ctLoopVariable = getForEachLoopVariable(
                        ctFor,
                        forLoopRange,
                        getPotentialLoopVariableAccess
                ).orElse(null);

                if (!iterable.equals(ctLoopVariable)) {
                    return;
                }


                addLocalProblem(
                        ctFor,
                        new LocalizedMessage(
                                "common-reimplementation",
                                Map.of(
                                        "suggestion", "for (%s value : %s) { ... }".formatted(
                                                elementType,
                                                iterableExpression
                                        )
                                )
                        ),
                        ProblemType.FOR_CAN_BE_FOREACH
                );
            }
        });
    }
}
