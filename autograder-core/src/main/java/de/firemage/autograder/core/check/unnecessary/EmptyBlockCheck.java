package de.firemage.autograder.core.check.unnecessary;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

@ExecutableCheck(reportedProblems = {ProblemType.EMPTY_BLOCK, ProblemType.EMPTY_CATCH})
public class EmptyBlockCheck extends IntegratedCheck {
    private static boolean isEmptyBlock(CtBlock<?> ctBlock) {
        return SpoonUtil.getEffectiveStatements(ctBlock).isEmpty()
            // allow empty blocks that only contain comments
            && (ctBlock.getStatements().isEmpty() || !ctBlock.getStatements().stream().allMatch(CtComment.class::isInstance));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtBlock(CtBlock<T> ctBlock) {
                if (ctBlock.isImplicit() || !ctBlock.getPosition().isValidPosition() || !isEmptyBlock(ctBlock)) {
                    super.visitCtBlock(ctBlock);
                    return;
                }

                if (ctBlock.getParent() instanceof CtMethod<?> ctMethod
                    && ctMethod.getBody().equals(ctBlock)
                    && MethodUtil.isInOverridingMethod(ctBlock)) {
                    return;
                }

                if (ctBlock.getParent() instanceof CtCatch ctCatch && ctCatch.getBody().equals(ctBlock)) {
                    addLocalProblem(
                        ctCatch,
                        new LocalizedMessage("empty-catch-block"),
                        ProblemType.EMPTY_CATCH
                    );
                } else {
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
