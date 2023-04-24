package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

@ExecutableCheck(reportedProblems = { ProblemType.MATH_FLOOR_DIVISION })
public class MathFloorDivision extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> ctInvocation) {
                boolean hasInvokedMathFloor = ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
                        // ensure the method is called on java.lang.Math
                        && ctInvocation.getFactory().Type().createReference(java.lang.Math.class)
                        .equals(ctTypeAccess.getAccessedType())
                        && ctInvocation.getExecutable().getSimpleName().equals("floor");
                if (!hasInvokedMathFloor) return;

                List<CtExpression<?>> args = ctInvocation.getArguments();
                if (args.size() != 1) return;

                CtExpression<?> arg = args.get(0);

                CtTypeReference<?> intType = ctInvocation.getFactory().Type().createReference(int.class);

                if (arg instanceof CtBinaryOperator<?> ctBinaryOperator
                    && ctBinaryOperator.getKind() == BinaryOperatorKind.DIV
                    && intType.equals(ctBinaryOperator.getLeftHandOperand().getType())
                    && intType.equals(ctBinaryOperator.getRightHandOperand().getType())
                ) {
                    addLocalProblem(
                        ctInvocation,
                        new LocalizedMessage("math-floor-division"),
                        ProblemType.MATH_FLOOR_DIVISION
                    );
                }
            }
        });
    }
}
