package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import java.util.List;

public class MethodShouldBeAbstractProcessor extends AbstractLoggingProcessor<CtClass<?>> {
    public MethodShouldBeAbstractProcessor(Check check) {
        super(check);
    }

    private static String formatExplanation(CtMethod<?> method) {
        return String.format("%s::%s should be abstract and not provide a default implementation", method.getDeclaringType().getQualifiedName(), method.getSimpleName());
    }

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

            List<CtStatement> statements = method.getBody().getStatements();
            if (statements.isEmpty()) {
                addProblem(method, formatExplanation(method));
            } else if (statements.size() == 1) {
                CtStatement statement = statements.get(0);
                if (statement instanceof CtReturn<?> ret
                        && ret.getReturnedExpression() instanceof CtLiteral<?> literal
                        && literal.getValue() == null) {
                    addProblem(method, formatExplanation(method));
                } else if (statement instanceof CtThrow ctThrow
                        && ctThrow.getThrownExpression() instanceof CtConstructorCall<?> call
                        && call.getType().getQualifiedName().equals("java.lang.UnsupportedOperationException")) {
                    addProblem(method, formatExplanation(method));
                }
            }
        }
    }
}
