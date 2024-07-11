package de.firemage.autograder.extra.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.extra.integrated.IdentifierNameUtils;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = {
    ProblemType.FIELD_SHOULD_BE_CONSTANT,
    ProblemType.LOCAL_VARIABLE_SHOULD_BE_CONSTANT
})
public class ConstantNamingAndQualifierCheck extends IntegratedCheck {
    private static final Set<String> IGNORE_FIELDS = Set.of("serialVersionUID");

    private static String getVisibilityString(CtModifiable ctModifiable) {
        ModifierKind modifierKind = ctModifiable.getVisibility();
        if (ctModifiable instanceof CtLocalVariable<?>) {
            return "private ";
        }

        if (modifierKind == null) {
            return "";
        }

        return modifierKind + " ";
    }

    private static String makeSuggestion(CtVariable<?> ctVariable) {
        CtTypeReference<?> ctVariableType = ctVariable.getType();

        return "%sstatic final %s %s = %s".formatted(
            getVisibilityString(ctVariable),
            ctVariableType.getSimpleName(),
            IdentifierNameUtils.toUpperSnakeCase(ctVariable.getSimpleName()),
            ctVariable.getDefaultExpression()
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> ctVariable) {
                // skip non-constant variables (and those that should be ignored)
                if (ctVariable.isImplicit()
                    || !ctVariable.getPosition().isValidPosition()
                    || !SpoonUtil.isEffectivelyFinal(ctVariable)
                    || ctVariable.getDefaultExpression() == null
                    || IGNORE_FIELDS.contains(ctVariable.getSimpleName())) {
                    return;
                }

                // only check primitive types and strings, because other types may be mutable like list
                // and should therefore not be static, even if they are final
                if (!ctVariable.getType().unbox().isPrimitive() && !SpoonUtil.isString(ctVariable.getType())) {
                    return;
                }

                if (ctVariable instanceof CtLocalVariable<?> ctLocalVariable
                    && SpoonUtil.resolveCtExpression(ctLocalVariable.getDefaultExpression()) instanceof CtLiteral<?>) {
                    // by the check above, ctLocalVariable has a default expression and is effectively final
                    //
                    // this code catches the case where one tries to bypass the checkstyle by doing:
                    // final int myLocalConstant = 0; instead of having a private static final...
                    addLocalProblem(
                        ctLocalVariable,
                        new LocalizedMessage("variable-should-be", Map.of(
                            "variable", ctLocalVariable.getSimpleName(),
                            "suggestion", makeSuggestion(ctLocalVariable)
                        )),
                        ProblemType.LOCAL_VARIABLE_SHOULD_BE_CONSTANT
                    );

                    return;
                }

                if (ctVariable instanceof CtField<?> ctField
                    && (!ctField.isStatic() || !IdentifierNameUtils.isUpperSnakeCase(ctField.getSimpleName()))) {
                    addLocalProblem(
                        ctField,
                        new LocalizedMessage("variable-should-be", Map.of(
                            "variable", ctField.getSimpleName(),
                            "suggestion", makeSuggestion(ctField)
                        )),
                        ProblemType.FIELD_SHOULD_BE_CONSTANT
                    );
                }
            }
        });
    }
}
