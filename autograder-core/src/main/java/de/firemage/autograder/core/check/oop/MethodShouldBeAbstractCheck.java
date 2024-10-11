package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION})
public class MethodShouldBeAbstractCheck extends IntegratedCheck {
    private static LocalizedMessage formatExplanation(CtMethod<?> method) {
        return new LocalizedMessage("method-should-be-abstract", Map.of(
            "type", method.getDeclaringType().getQualifiedName(),
            "method", method.getSimpleName()
        ));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctClass) {
                if (ctClass.isImplicit() || !ctClass.isAbstract()) {
                    return;
                }

                for (CtMethod<?> method : ctClass.getMethods()) {
                    if (!method.isPublic() && !method.isProtected()
                        || method.isStatic()
                        || method.isAbstract()
                        // skip methods that override another method
                        || MethodHierarchy.isOverridingMethod(method)
                        // skip methods that are never overridden (would never make sense to be abstract)
                        || !MethodHierarchy.isOverriddenMethod(method)
                        || MethodUtil.hasBeenInvoked(method)) {
                        continue;
                    }

                    List<CtStatement> statements = StatementUtil.getEffectiveStatements(method.getBody());
                    if (statements.isEmpty()) {
                        addLocalProblem(method, formatExplanation(method),
                            ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION);
                        return;
                    }

                    if (statements.size() != 1) {
                        return;
                    }

                    CtStatement statement = statements.get(0);
                    if (statement instanceof CtReturn<?> ret
                        && ret.getReturnedExpression() instanceof CtLiteral<?> literal
                        && literal.getValue() == null) {
                        addLocalProblem(method, formatExplanation(method),
                            ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION);
                    } else if (statement instanceof CtThrow ctThrow
                        && ctThrow.getThrownExpression() instanceof CtConstructorCall<?> call
                        && TypeUtil.isTypeEqualTo(call.getType(), java.lang.UnsupportedOperationException.class, java.lang.IllegalStateException.class)) {
                        addLocalProblem(method, formatExplanation(method),
                            ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION);
                    }
                }
            }
        });
    }
}
