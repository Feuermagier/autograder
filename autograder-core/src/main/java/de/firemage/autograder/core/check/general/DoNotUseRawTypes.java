package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;

import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.DO_NOT_USE_RAW_TYPES })
public class DoNotUseRawTypes extends IntegratedCheck {
    private boolean isRawType(CtTypeReference<?> ctTypeReference) {
        CtType<?> declaration = ctTypeReference.getTypeDeclaration();

        if (declaration == null) {
            // reference points to a type not in the class-path
            return false;
        }

        if (ctTypeReference.getRoleInParent() == CtRole.DECLARING_TYPE) {
            // Prevent 'Map' in 'Map.Entry<A, B>' from being reported
            return false;
        }

        return declaration.getFormalCtTypeParameters().size() != ctTypeReference.getActualTypeArguments().size();
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtTypeReference<?>>() {
            @Override
            public void process(CtTypeReference<?> ctTypeReference) {
                // skip references which have no position in source code
                if (!ctTypeReference.getPosition().isValidPosition()) {
                    return;
                }

                if (isRawType(ctTypeReference)) {
                    addLocalProblem(
                        ctTypeReference,
                        new LocalizedMessage("do-not-use-raw-types-exp"),
                        ProblemType.DO_NOT_USE_RAW_TYPES
                    );
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(4);
    }
}
