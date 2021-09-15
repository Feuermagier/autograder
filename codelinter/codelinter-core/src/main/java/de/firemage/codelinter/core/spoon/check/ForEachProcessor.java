package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.QueueProcessingManager;

import java.util.List;

/**
 * Checks for 'for' loops that could be converted to a for-each loop
 * Note: This class is not finished and doesn't work correctly. Maybe I will finish it later, but for now PMD will
 * check for for loops that can be converted
 */
public class ForEachProcessor extends AbstractLoggingProcessor<CtFor> {
    private static final String DESCRIPTION = "For loop should be replaced by a for-each loop";
    private static final String EXPLANATION = """
            """;

    private static final List<String> ITERABLE_CLASSES = List.of("java.util.List", "java.util.ArrayList", "java.util.LinkedList");

    public ForEachProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtFor loop) {
        var loopVariable = checkInit(loop);

        if (loopVariable != null && checkUpdate(loop, loopVariable)) {
            var guard = checkGuard(loop, loopVariable);
        }
    }

    private CtVariableReference<?> checkInit(CtFor loop) {
        if (loop.getForInit().size() == 1) {
            if (loop.getForInit().get(0) instanceof CtLocalVariable<?> loopVariable
                    && (loopVariable.getType().getSimpleName().equals("int")
                    || loopVariable.getType().getSimpleName().equals("Integer"))) {
                return loopVariable.getReference();
            }
        }
        return null;
    }

    private boolean checkUpdate(CtFor loop, CtVariableReference<?> loopVariable) {
        if (loop.getForUpdate().isEmpty()) {
            return false;
        }
        // We only check for x++ and ++x, not x += 1 or x = x + 1
        return loop.getForUpdate().get(0) instanceof CtUnaryOperator<?> operator
                && (operator.getKind().equals(UnaryOperatorKind.POSTINC)
                || operator.getKind().equals(UnaryOperatorKind.PREINC))
                && operator.getOperand() instanceof CtVariableWrite<?> access
                && access.getVariable().equals(loopVariable);
    }

    private CtVariableReference<?> checkGuard(CtFor loop, CtVariableReference<?> loopVariable) {
        if (loop.getExpression() instanceof CtBinaryOperator<?> operator
                && operator.getKind().equals(BinaryOperatorKind.LT)
                && operator.getLeftHandOperand() instanceof CtVariableRead<?> lhs
                && lhs.getVariable().equals(loopVariable)) {

            // RHS: Array length check
            if (operator.getRightHandOperand() instanceof CtFieldRead<?> read
                    && read.getVariable().getSimpleName().equals("length")
                    && read.getTarget() instanceof CtVariableRead<?> target
                    && target.getVariable().getType().isArray()) {
                return target.getVariable();
                // RHS: Iterable size check
            } else if (operator.getRightHandOperand() instanceof CtInvocation<?> invocation
                    && invocation.getExecutable().getSignature().equals("size()")
                    && invocation.getTarget() instanceof CtVariableRead<?> target
                    && ITERABLE_CLASSES.contains(target.getVariable().getType().getQualifiedName())) {
                return target.getVariable();
            }

        }
        return null;
    }

    private boolean checkIndexUses(CtFor loop, CtVariableReference<?> index, CtVariableReference<?> iterable) {
        QueueProcessingManager processingManager = new QueueProcessingManager(getFactory());
        boolean otherIndexUse = false;
        processingManager.addProcessor(new AbstractProcessor<CtVariableAccess<?>>() {
            @Override
            public void process(CtVariableAccess<?> access) {
                // List get
                if (!(access.getVariable().equals(index)
                        && access.getParent() instanceof CtInvocation<?> invocation
                        && invocation.getExecutable().getSignature().equals("get(int)")
                        && invocation.getTarget() instanceof CtVariableRead<?> target
                        && target.getVariable().equals(iterable))) {
                    // Signal violation
                }
            }
        });
        processingManager.process(loop.getBody());
        return otherIndexUse;
    }
}
