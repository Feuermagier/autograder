package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

@ExecutableCheck(reportedProblems = { ProblemType.USE_ENUM_COLLECTION })
public class UseEnumCollection extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructorCall<?>>() {
            @Override
            public void process(CtConstructorCall<?> ctConstructorCall) {
                if (ctConstructorCall.isImplicit() || !ctConstructorCall.getPosition().isValidPosition()) return;

                TypeFactory typeFactory = ctConstructorCall.getFactory().Type();
                CtTypeReference<?> type = ctConstructorCall.getType();
                if ((
                        type.isSubtypeOf(typeFactory.createReference(java.util.HashMap.class))
                        || type.isSubtypeOf(typeFactory.createReference(java.util.HashSet.class))
                ) && type.getActualTypeArguments().stream().findFirst().map(CtTypeInformation::isEnum).orElse(false)) {
                    addLocalProblem(
                        ctConstructorCall,
                        new LocalizedMessage("use-enum-collection"),
                        ProblemType.USE_ENUM_COLLECTION
                    );
                }
            }
        });
    }
}
