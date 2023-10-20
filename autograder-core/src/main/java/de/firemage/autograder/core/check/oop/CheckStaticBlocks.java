package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtAnonymousExecutable;
import spoon.reflect.visitor.filter.TypeFilter;


@ExecutableCheck(reportedProblems = {ProblemType.AVOID_STATIC_BLOCKS})
public class CheckStaticBlocks extends IntegratedCheck {
    public static final String LOCALIZED_MESSAGE_KEY = "avoid-static-blocks";
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().getElements(new TypeFilter<>(CtBlock.class)).forEach(block -> {
            if (block.getParent() instanceof CtAnonymousExecutable executable && executable.isStatic()) {
                this.addLocalProblem(
                        block,
                        new LocalizedMessage(LOCALIZED_MESSAGE_KEY),
                        ProblemType.AVOID_STATIC_BLOCKS
                );
            }
        });
    }
}
