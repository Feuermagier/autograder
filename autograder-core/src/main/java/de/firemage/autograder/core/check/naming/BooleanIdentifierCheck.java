package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.BOOLEAN_GETTER_NOT_CALLED_IS})
public class BooleanIdentifierCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtMethod<?>>() {
            @Override
            public void process(CtMethod<?> ctMethod) {
                if (ctMethod.isImplicit() || !ctMethod.getPosition().isValidPosition()) {
                    return;
                }

                String methodName = ctMethod.getSimpleName();
                if (ctMethod.getType().equals(ctMethod.getFactory().Type().booleanPrimitiveType())
                    && methodName.startsWith("get")) {
                    String newName = "is" + methodName.substring(3);
                    addLocalProblem(
                        ctMethod.getType(),
                        new LocalizedMessage("bool-getter-name", Map.of(
                            "newName", newName,
                            "oldName", methodName
                        )),
                        ProblemType.BOOLEAN_GETTER_NOT_CALLED_IS
                    );
                }
            }
        });
    }
}
