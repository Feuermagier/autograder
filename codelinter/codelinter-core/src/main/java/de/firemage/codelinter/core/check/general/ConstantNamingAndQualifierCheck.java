package de.firemage.codelinter.core.check.general;

import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.integrated.IdentifierNameUtils;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.integrated.SpoonUtil;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;

public class ConstantNamingAndQualifierCheck extends IntegratedCheck {
    private static final String DESCRIPTION =
        "Constants that are never written to should be 'static final' and have a UPPER_SNAKE_CASE name";

    public ConstantNamingAndQualifierCheck() {
        super(DESCRIPTION);
    }

    private static String formatExplanation(CtField<?> field) {
        return String.format("The constant field '%s' of class %s should be static and have a UPPER_SNAKE_CASE name",
            field.getSimpleName(), field.getDeclaringType().getQualifiedName());
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
                        addLocalProblem(field, formatExplanation(field));
                    }
                }
            }
        });
    }
}
