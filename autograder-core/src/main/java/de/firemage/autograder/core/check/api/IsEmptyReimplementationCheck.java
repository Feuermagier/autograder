package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;

import java.util.Map;
import java.util.function.Predicate;

@ExecutableCheck(reportedProblems = {
    ProblemType.COLLECTION_IS_EMPTY_REIMPLEMENTED,
    ProblemType.STRING_IS_EMPTY_REIMPLEMENTED
})
public class IsEmptyReimplementationCheck extends IntegratedCheck {
    private void reportProblem(CtElement ctElement, String original, String suggestion, ProblemType problemType) {
        this.addLocalProblem(
            ctElement,
            new LocalizedMessage("suggest-replacement", Map.of("original", original, "suggestion", suggestion)),
            problemType
        );
    }

    private static boolean isTargetTypeEqualTo(CtInvocation<?> ctInvocation, Class<?> ctClass) {
        return ctInvocation.getTarget() != null && SpoonUtil.isTypeEqualTo(ctInvocation.getTarget().getType(), ctClass);
    }

    private static boolean isSizeCall(CtInvocation<?> ctInvocation) {
        CtExpression<?> target = ctInvocation.getTarget();
        return target != null
            && target.getType().getTypeDeclaration() != null
            // the type with the size method must have an isEmpty method
            && target.getType().getTypeDeclaration().getMethod(
                ctInvocation.getFactory().Type().booleanPrimitiveType(),
                "isEmpty"
            ) != null
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), int.class, "size");
    }

    private static boolean isLengthCall(CtInvocation<?> ctInvocation) {
        return isTargetTypeEqualTo(ctInvocation, java.lang.String.class)
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), int.class, "length");
    }

    private static boolean isEqualsCall(CtInvocation<?> ctInvocation) {
        return isTargetTypeEqualTo(ctInvocation, java.lang.String.class)
            && SpoonUtil.isSignatureEqualTo(ctInvocation.getExecutable(), boolean.class, "equals", Object.class);
    }

    private static CtExpression<?> buildIsEmptySuggestion(CtExpression<?> target) {
        return target.getFactory().createInvocation(
            target.clone(),
            target.getType()
                .getTypeDeclaration()
                .getMethod("isEmpty")
                .getReference()
                .clone()
        );
    }

    private void checkIsEmptyReimplementation(CtExpression<?> target, CtBinaryOperator<?> ctBinaryOperator, ProblemType problemType) {
        Predicate<? super CtExpression<?>> isLiteral = expr -> expr instanceof CtLiteral<?>;

        if (!SpoonUtil.isBoolean(ctBinaryOperator)) {
            return;
        }

        // swap operator if necessary, so that the literal is on the right side:
        // <expr> <op> <literal>
        //
        // Those are all combinations if the literal is on the right side:
        //
        // f() < 0     : ignore
        // f() < 1     : isEmpty
        // f() <= 0    : isEmpty
        // f() <= 1    : ignore
        //
        // f() > 0     : !isEmpty
        // f() > 1     : ignore
        // f() >= 0    : ignore
        // f() >= 1    : !isEmpty
        //
        // f() != 0    : !isEmpty
        // f() != 1    : ignore
        // f() == 0    : isEmpty
        // f() == 1    : ignore
        //
        // After normalization the combinations are reduced to:
        //
        // f() <= -1   : ignore
        // f() <= 0    : isEmpty
        // f() <= 1    : ignore
        //
        // f() >= 2    : ignore
        // f() >= 0    : ignore
        // f() >= 1    : !isEmpty
        //
        // f() != 0    : !isEmpty
        // f() != 1    : ignore
        // f() == 0    : isEmpty
        // f() == 1    : ignore
        //
        // The only combinations that are not ignored are:
        // f() <= 0    : isEmpty
        // f() >= 1    : !isEmpty
        // f() != 0    : !isEmpty
        // f() == 0    : isEmpty


        CtBinaryOperator<?> result = SpoonUtil.normalizeBy(
            (left, right) -> isLiteral.test(left) && !isLiteral.test(right),
            ctBinaryOperator
        );

        if (!(result.getRightHandOperand() instanceof CtLiteral<?> ctLiteral) || !(ctLiteral.getValue() instanceof Number number)) {
            return;
        }

        boolean isZero = number.doubleValue() == 0.0;
        boolean isOne = number.doubleValue() == 1.0;

        CtExpression<?> suggestion = buildIsEmptySuggestion(target);

        switch (result.getKind()) {
            // f() == 0    : isEmpty
            // f() <= 0    : isEmpty
            case EQ, LE -> {
                if (isZero) {
                    this.reportProblem(ctBinaryOperator, ctBinaryOperator.toString(), suggestion.toString(), problemType);
                }
            }
            // f() != 0    : !isEmpty
            case NE -> {
                if (isZero) {
                    this.reportProblem(ctBinaryOperator, ctBinaryOperator.toString(), SpoonUtil.negate(suggestion).toString(), problemType);
                }
            }
            // f() >= 1    : !isEmpty
            case GE -> {
                if (isOne) {
                    this.reportProblem(ctBinaryOperator, ctBinaryOperator.toString(), SpoonUtil.negate(suggestion).toString(), problemType);
                }
            }
        }
    }

    private void checkEqualsCall(CtExpression<?> target, CtInvocation<?> ctInvocation, ProblemType problemType) {
        CtExpression<?> argument = ctInvocation.getArguments().get(0);
        if (SpoonUtil.isStringLiteral(SpoonUtil.resolveConstant(argument), "")) {
            this.reportProblem(ctInvocation, ctInvocation.toString(), buildIsEmptySuggestion(target).toString(), problemType);
        }

        // detect "".equals(s)
        if (SpoonUtil.isStringLiteral(SpoonUtil.resolveConstant(target), "")) {
            this.reportProblem(ctInvocation, ctInvocation.toString(), buildIsEmptySuggestion(argument).toString(), problemType);
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                if (ctInvocation.isImplicit()
                    || !ctInvocation.getPosition().isValidPosition()) {
                    return;
                }

                CtExpression<?> target = ctInvocation.getTarget();
                if (target == null) {
                    return;
                }

                if (isEqualsCall(ctInvocation)) {
                    checkEqualsCall(target, ctInvocation, ProblemType.STRING_IS_EMPTY_REIMPLEMENTED);
                }

                if (!(ctInvocation.getParent() instanceof CtBinaryOperator<?> ctBinaryOperator)) {
                    return;
                }

                if (isLengthCall(ctInvocation)) {
                    checkIsEmptyReimplementation(target, ctBinaryOperator, ProblemType.STRING_IS_EMPTY_REIMPLEMENTED);
                }

                if (isSizeCall(ctInvocation)) {
                    checkIsEmptyReimplementation(target, ctBinaryOperator, ProblemType.COLLECTION_IS_EMPTY_REIMPLEMENTED);
                }
            }
        });
    }
}
