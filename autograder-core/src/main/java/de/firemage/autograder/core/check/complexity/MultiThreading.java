package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.CodePositionImpl;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtSynchronized;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.CtExtendedModifier;

@ExecutableCheck(reportedProblems = { ProblemType.MULTI_THREADING })
public class MultiThreading extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public void visitCtSynchronized(CtSynchronized ctSynchronized) {
                if (ctSynchronized.isImplicit() || !ctSynchronized.getPosition().isValidPosition()) {
                    super.visitCtSynchronized(ctSynchronized);
                    return;
                }

                addLocalProblem(
                    ctSynchronized.getExpression(),
                    new LocalizedMessage("multi-threading"),
                    ProblemType.MULTI_THREADING
                );

                super.visitCtSynchronized(ctSynchronized);
            }

            @Override
            protected void enter(CtElement ctElement) {
                if (!ctElement.isImplicit()
                    && ctElement.getPosition().isValidPosition()
                    && ctElement instanceof CtModifiable ctModifiable) {
                    for (CtExtendedModifier modifier : ctModifiable.getExtendedModifiers()) {
                        if (modifier.isImplicit() || !modifier.getPosition().isValidPosition()) {
                            continue;
                        }

                        if (modifier.getKind() == ModifierKind.SYNCHRONIZED) {
                            addLocalProblem(
                                CodePositionImpl.fromSourcePosition(modifier.getPosition(), ctElement, getRoot()),
                                new LocalizedMessage("multi-threading"),
                                ProblemType.MULTI_THREADING
                            );
                        }
                    }
                }
            }
        });
    }
}
