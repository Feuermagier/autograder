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
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;

import java.util.Map;
import java.util.Set;

@ExecutableCheck(reportedProblems = {ProblemType.SINGLE_LETTER_LOCAL_NAME, ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE})
public class VariablesHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final Set<String> ALLOWED_ABBREVIATIONS = Set.of("ui");

    private static final Set<String> ALLOWED_OBJ_NAMES_IN_EQUALS = Set.of("o", "obj", "other", "object");

    public VariablesHaveDescriptiveNamesCheck() {
        super(new LocalizedMessage("variable-name-desc"));
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
                    addLocalProblem(variable, new LocalizedMessage("variable-name-exp-single-letter",
                            Map.of("name", variable.getSimpleName())),
                        ProblemType.SINGLE_LETTER_LOCAL_NAME);
                } else if (isTypeAbbreviation(variable)) {
                    addLocalProblem(variable,
                        new LocalizedMessage("variable-name-exp-type", Map.of("name", variable.getSimpleName())),
                        ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE);
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
