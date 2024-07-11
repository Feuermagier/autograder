package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
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
        return new LocalizedMessage("method-abstract-exp", Map.of(
            "type", method.getDeclaringType().getQualifiedName(),
            "method", method.getSimpleName()
        ));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> clazz) {
                if (!clazz.isAbstract()) {
                    return;
                }

                for (CtMethod<?> method : clazz.getMethods()) {
                    if (!method.isPublic() && !method.isProtected()) {
                        continue;
                    }

                    // False positives if the method overrides another method but is not correctly annotated
                    if (method.isStatic() || method.isAbstract() || method.hasAnnotation(Override.class)) {
                        continue;
                    }

                    List<CtStatement> statements = SpoonUtil.getEffectiveStatements(method.getBody());
                    if (statements.isEmpty()) {
                        addLocalProblem(method, formatExplanation(method),
                            ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION);
                    } else if (statements.size() == 1) {
                        CtStatement statement = statements.get(0);
                        if (statement instanceof CtReturn<?> ret
                            && ret.getReturnedExpression() instanceof CtLiteral<?> literal
                            && literal.getValue() == null) {
                            addLocalProblem(method, formatExplanation(method),
                                ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION);
                        } else if (statement instanceof CtThrow ctThrow
                            && ctThrow.getThrownExpression() instanceof CtConstructorCall<?> call) {
                            String type = call.getType().getQualifiedName();
                            if (type.equals("java.lang.UnsupportedOperationException") ||
                                type.equals("java.lang.IllegalStateException")) {
                                addLocalProblem(method, formatExplanation(method),
                                    ProblemType.METHOD_USES_PLACEHOLDER_IMPLEMENTATION);
                            }
                        }
                    }
                }
            }
        });
    }
}
