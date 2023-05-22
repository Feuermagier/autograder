package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.visitor.CtScanner;

@ExecutableCheck(reportedProblems = {ProblemType.EMPTY_BLOCK})
public class EmptyNonCatchBlockCheck extends IntegratedCheck {
    private static boolean isInAllowedContext(CtBlock<?> ctBlock) {
        return ctBlock.getParent() instanceof CtCatch ctCatch && ctCatch.getBody().equals(ctBlock);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtBlock(CtBlock<T> ctBlock) {
                if (ctBlock.isImplicit() || !ctBlock.getPosition().isValidPosition() || isInAllowedContext(ctBlock)) {
                    super.visitCtBlock(ctBlock);
                    return;
                }

                if (SpoonUtil.getEffectiveStatements(ctBlock).isEmpty()
                    // allow empty blocks that only contain comments
                    && (ctBlock.getStatements().isEmpty() || !ctBlock.getStatements().stream().allMatch(CtComment.class::isInstance))) {
                    addLocalProblem(
                        ctBlock,
                        new LocalizedMessage("empty-block"),
                        ProblemType.EMPTY_BLOCK
                    );
                }

                super.visitCtBlock(ctBlock);
            }

            @Override
            public <T> void visitCtSwitch(CtSwitch<T> ctSwitch) {
                if (ctSwitch.getCases().isEmpty()) {
                    addLocalProblem(
                        ctSwitch,
                        new LocalizedMessage("empty-block"),
                        ProblemType.EMPTY_BLOCK
                    );
                }

                super.visitCtSwitch(ctSwitch);
            }
        });
    }
}
