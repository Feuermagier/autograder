package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.ForLoopRange;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtOperatorAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.DirectReferenceFilter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {
    ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY,
    ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT
})
public class CommonReimplementation extends IntegratedCheck {
    private void checkStringRepeat(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1 || forLoopRange == null) {
            return;
        }

        // lhs += rhs
        if (statements.get(0) instanceof CtOperatorAssignment<?, ?> ctAssignment
            && ctAssignment.getKind() == BinaryOperatorKind.PLUS) {
            CtExpression<?> lhs = ctAssignment.getAssigned();
            if (!SpoonUtil.isTypeEqualTo(lhs.getType(), String.class)) {
                return;
            }

            CtExpression<?> rhs = SpoonUtil.resolveCtExpression(ctAssignment.getAssignment());
            // return if the for loop uses the loop variable (would not be a simple repetition)
            if (!ctAssignment.getElements(new DirectReferenceFilter<>(forLoopRange.loopVariable())).isEmpty()) {
                return;
            }

            // return if the rhs uses the lhs: lhs += rhs + lhs
            if (lhs instanceof CtVariableAccess<?> ctVariableAccess && !rhs.getElements(new DirectReferenceFilter<>(ctVariableAccess.getVariable())).isEmpty()) {
                return;
            }

            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        // string.repeat(count)
                        "suggestion", "%s += %s".formatted(
                            lhs.prettyprint(),
                            rhs.getFactory().createInvocation(
                                rhs,
                                rhs.getFactory().Type().get(java.lang.String.class)
                                    .getMethod("repeat", rhs.getFactory().createCtTypeReference(int.class))
                                    .getReference(),
                                forLoopRange.length()
                            ).prettyprint())
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_STRING_REPEAT
            );
        }
    }

    private void checkArrayCopy(CtFor ctFor) {
        ForLoopRange forLoopRange = ForLoopRange.fromCtFor(ctFor).orElse(null);

        List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctFor.getBody());
        if (statements.size() != 1 || forLoopRange == null) {
            return;
        }


        if (statements.get(0) instanceof CtAssignment<?, ?> ctAssignment
            && !(ctAssignment instanceof CtOperatorAssignment<?, ?>)
            && ctAssignment.getAssigned() instanceof CtArrayAccess<?, ?> lhs
            && ctAssignment.getAssignment() instanceof CtArrayAccess<?, ?> rhs
            && lhs.getTarget() != null
            && rhs.getTarget() != null
            && lhs.getIndexExpression().equals(rhs.getIndexExpression())
            && lhs.getIndexExpression() instanceof CtVariableRead<Integer> index
            && index.getVariable().equals(forLoopRange.loopVariable())) {
            this.addLocalProblem(
                ctFor,
                new LocalizedMessage(
                    "common-reimplementation",
                    Map.of(
                        // System.arraycopy(src, srcPos, dest, destPos, length)
                        "suggestion", "System.arraycopy(%s, %s, %s, %s, %s)".formatted(
                            rhs.getTarget().prettyprint(),
                            forLoopRange.start().prettyprint(),
                            lhs.getTarget().prettyprint(),
                            forLoopRange.start().prettyprint(),
                            forLoopRange.length().prettyprint()
                        )
                    )
                ),
                ProblemType.COMMON_REIMPLEMENTATION_ARRAY_COPY
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtFor(CtFor ctFor) {
                if (ctFor.isImplicit() || !ctFor.getPosition().isValidPosition()) {
                    super.visitCtFor(ctFor);
                    return;
                }

                checkArrayCopy(ctFor);
                checkStringRepeat(ctFor);
                super.visitCtFor(ctFor);
            }
        });
    }
}
