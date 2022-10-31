package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IdentifierNameUtils;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;

import java.util.Map;

public class ConstantNamingAndQualifierCheck extends IntegratedCheck {
    public ConstantNamingAndQualifierCheck() {
        super(new LocalizedMessage("constant-naming-qualifier-desc"));
    }

    private static LocalizedMessage formatExplanation(CtField<?> field) {
        return new LocalizedMessage("constant-naming-qualifier-exp", Map.of(
            "field", field.getSimpleName(),
            "class", field.getDeclaringType().getQualifiedName()
        ));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (field.isFinal()
                    && (field.getType().unbox().isPrimitive() || SpoonUtil.isString(field.getType()))
                    && field.getDefaultExpression() != null) {
                    if (!field.isStatic() || !IdentifierNameUtils.isUpperSnakeCase(field.getSimpleName())) {
                        addLocalProblem(field, formatExplanation(field),
                            ProblemType.CONSTANT_NOT_STATIC_OR_NOT_UPPER_CAMEL_CASE);
                    }
                }
            }
        });
    }
}
