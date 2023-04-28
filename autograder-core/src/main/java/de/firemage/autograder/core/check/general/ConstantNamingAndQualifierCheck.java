package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IdentifierNameUtils;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtField;

import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = {ProblemType.CONSTANT_NOT_STATIC_OR_NOT_UPPER_CAMEL_CASE})
public class ConstantNamingAndQualifierCheck extends IntegratedCheck {
    private static final Set<String> IGNORE_FIELDS = Set.of("serialVersionUID");

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
                    && field.getDefaultExpression() != null && !IGNORE_FIELDS.contains(field.getSimpleName())) {
                    if (!field.isStatic() || !IdentifierNameUtils.isUpperSnakeCase(field.getSimpleName())) {
                        addLocalProblem(field, formatExplanation(field),
                            ProblemType.CONSTANT_NOT_STATIC_OR_NOT_UPPER_CAMEL_CASE);
                    }
                }
            }
        });
    }
}
