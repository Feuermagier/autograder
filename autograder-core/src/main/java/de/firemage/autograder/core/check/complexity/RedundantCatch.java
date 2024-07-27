package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.reference.CtVariableReference;

import java.util.List;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_CATCH })
public class RedundantCatch extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtCatch>() {
            @Override
            public void process(CtCatch ctCatch) {
                if (ctCatch.isImplicit() || !ctCatch.getPosition().isValidPosition()) return;

                List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctCatch.getBody());
                Optional<Effect> singleEffect = StatementUtil.getSingleEffect(statements);

                CtVariableReference<?> caughtVariable = ctCatch.getParameter().getReference();

                singleEffect.ifPresent(
                    effect -> {
                        if (effect instanceof TerminalEffect terminalEffect
                            && terminalEffect.isThrow()
                            && terminalEffect.value().isPresent()
                            && terminalEffect.value().get() instanceof CtVariableRead<?> variableRead
                            && variableRead.getVariable().equals(caughtVariable)) {
                            addLocalProblem(
                                effect.ctStatement(),
                                new LocalizedMessage("redundant-catch"),
                                ProblemType.REDUNDANT_CATCH
                            );
                        }
                    }
                );
            }
        });
    }
}
