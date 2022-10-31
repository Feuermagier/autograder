package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.apache.commons.lang3.StringUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;

public class VariablesHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final String DESCRIPTION = "Local variables must have descriptive names";


    public VariablesHaveDescriptiveNamesCheck() {
        super(DESCRIPTION);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> variable) {
                if (variable instanceof CtCatchVariable || variable.getParent() instanceof CtLambda) {
                    // Catch vars and lambda vars have less strict rules, e.g. it is ok to write "Exception e" or "NullPointerException npe"
                    return;
                }

                if (variable.getSimpleName().equals("o")
                    && variable.getParent() instanceof CtMethod<?> method
                    && (SpoonUtil.isEqualsMethod(method) || SpoonUtil.isCompareToMethod(method))) {
                    // The parameter of the equals and compareTo methods may be named "o"
                    return;
                }

                if (variable.getSimpleName().length() == 1
                    && !isAllowedLoopCounter(variable)
                    && !isCoordinate(variable)) {
                    addLocalProblem(variable, "Single letter names are usually non-descriptive",
                        ProblemType.SINGLE_LETTER_LOCAL_NAME);
                } else if (isTypeAbbreviation(variable)) {
                    addLocalProblem(variable, "Don't use unnecessary abbreviations",
                        ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE);
                }
            }
        });
    }

    private boolean isCoordinate(CtVariable<?> variable) {
        return (variable.getSimpleName().equals("x") || variable.getSimpleName().equals("y"));
    }

    private boolean isAllowedLoopCounter(CtVariable<?> variable) {
        return variable.getRoleInParent() == CtRole.FOR_INIT && SpoonUtil.isPrimitiveNumeric(variable.getType());
    }

    private boolean isTypeAbbreviation(CtVariable<?> variable) {
        if (variable.getType().isPrimitive()) {
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
