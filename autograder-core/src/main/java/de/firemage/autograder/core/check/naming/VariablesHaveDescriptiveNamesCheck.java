package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.apache.commons.lang3.StringUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = {ProblemType.SINGLE_LETTER_LOCAL_NAME, ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE, ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME})
public class VariablesHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final Set<String> ALLOWED_ABBREVIATIONS = Set.of("ui");

    private static final Set<String> ALLOWED_OBJ_NAMES_IN_EQUALS = Set.of("o", "obj", "other", "object");
    private static final List<String> TYPE_NAMES = List.of(
        "string", "list", "array", "map", "set", "int", "long", "float"
    );

    private static boolean hasTypeInName(CtNamedElement ctVariable) {
        String name = ctVariable.getSimpleName().toLowerCase();

        return TYPE_NAMES.stream().anyMatch(ty -> name.contains(ty) && !name.equals(ty));
    }

    private void reportProblem(String key, CtNamedElement ctVariable, ProblemType problemType) {
        this.addLocalProblem(
            ctVariable,
            new LocalizedMessage(key, Map.of("name", ctVariable.getSimpleName())),
            problemType
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> variable) {
                if (variable instanceof CtCatchVariable || isLambdaParameter(variable)) {
                    // Catch vars and lambda vars have less strict rules, e.g. it is ok to write "Exception e" or "NullPointerException npe"
                    return;
                }

                if (ALLOWED_OBJ_NAMES_IN_EQUALS.contains(variable.getSimpleName())
                    && variable.getParent() instanceof CtMethod<?> method
                    && (SpoonUtil.isEqualsMethod(method) || SpoonUtil.isCompareToMethod(method))) {
                    // The parameter of the equals and compareTo methods may be named "o", "obj", ...
                    return;
                }

                if (variable.getSimpleName().length() == 1
                    && !isAllowedLoopCounter(variable)
                    && !isCoordinate(variable)) {
                    reportProblem("variable-name-exp-single-letter", variable, ProblemType.SINGLE_LETTER_LOCAL_NAME);
                } else if (isTypeAbbreviation(variable)) {
                    reportProblem("variable-name-exp-type", variable, ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE);
                } else if (hasTypeInName(variable)) {
                    reportProblem("variable-name-exp-type-in-name", variable, ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME);
                }
            }
        });
    }

    private boolean isLambdaParameter(CtVariable<?> variable) {
        return variable instanceof CtParameter<?> && variable.getParent() instanceof CtLambda<?>;
    }

    private boolean isCoordinate(CtVariable<?> variable) {
        return (variable.getSimpleName().equals("x") || variable.getSimpleName().equals("y"));
    }

    private boolean isAllowedLoopCounter(CtVariable<?> variable) {
        return (variable.getRoleInParent() == CtRole.FOR_INIT || variable.getRoleInParent() == CtRole.FOREACH_VARIABLE)
            && SpoonUtil.isPrimitiveNumeric(variable.getType());
    }

    private boolean isTypeAbbreviation(CtVariable<?> variable) {
        if (variable.getType().isPrimitive()) {
            return false;
        }

        if (ALLOWED_ABBREVIATIONS.contains(variable.getSimpleName().toLowerCase())) {
            return false;
        }

        String name = variable.getSimpleName();
        String type = variable.getType().getSimpleName();
        String[] parts = StringUtils.splitByCharacterTypeCamelCase(type);

        if (parts[0].length() >= 4 && name.length() <= 3 && parts[0].toLowerCase().indexOf(name) == 0) {
            // Scanner -> sc
            return true;
        } else if (parts.length == name.length()) {
            // MyCoolClass -> mcc
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].toLowerCase().charAt(0) != name.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
