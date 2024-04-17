package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.reference.CtTypeReference;

import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.IMPLEMENT_COMPARABLE })

public class ImplementComparable extends IntegratedCheck {
    private static CtTypeReference<?> getInterface(CtTypeInformation ctType, Class<?> interfaceType) {
        return ctType.getSuperInterfaces()
            .stream()
            .filter(type -> SpoonUtil.isSubtypeOf(type, interfaceType))
            .findAny()
            .orElse(null);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctType) {
                if (ctType.isImplicit()
                    || !ctType.getPosition().isValidPosition()) {
                    return;
                }

                CtTypeReference<?> comparator = getInterface(ctType, java.util.Comparator.class);
                if (comparator == null || comparator.getActualTypeArguments().size() != 1) {
                    return;
                }

                // get the type for which comparator is implemented
                CtType<?> compared = comparator.getActualTypeArguments().get(0).getTypeDeclaration();

                // if that type does not implement comparable, it should implement it
                if (ctType.equals(compared)) {
                    addLocalProblem(
                        ctType,
                        new LocalizedMessage(
                            "implement-comparable",
                            Map.of("name", compared.getSimpleName())
                        ),
                        ProblemType.IMPLEMENT_COMPARABLE
                    );
                }
            }
        });
    }
}
