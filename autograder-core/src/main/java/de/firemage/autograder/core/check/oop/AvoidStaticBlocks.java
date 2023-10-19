package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.visitor.CtScanner;

@ExecutableCheck(reportedProblems = { ProblemType.STATIC_BLOCKS })
public class AvoidStaticBlocks extends IntegratedCheck {
    public static final String LOCALIZED_MESSAGE_KEY = "avoid-static-blocks";
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <R> void visitCtBlock(CtBlock<R> block) {
                if (block.getParent() instanceof CtAnonymousExecutable executable && executable.isStatic()) {
                    addLocalProblem(
                        block,
                        new LocalizedMessage(LOCALIZED_MESSAGE_KEY),
                        ProblemType.STATIC_BLOCKS
                    );
                }

            }
        });

    }
}
