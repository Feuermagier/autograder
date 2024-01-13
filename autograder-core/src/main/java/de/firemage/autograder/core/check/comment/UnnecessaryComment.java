package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;

@ExecutableCheck(reportedProblems = { ProblemType.UNNECESSARY_COMMENT })
public class UnnecessaryComment extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment ctComment) {
                if (ctComment.isImplicit()
                    || !ctComment.getPosition().isValidPosition()
                    || ctComment.getCommentType() != CtComment.CommentType.INLINE) {
                    return;
                }

                CtStatement previous = SpoonUtil.getPreviousStatement(ctComment).orElse(null);
                if (previous instanceof CtComment) {
                    return;
                }

                if (ctComment.getContent().isBlank()) {
                    addLocalProblem(
                        ctComment,
                        new LocalizedMessage("unnecessary-comment-empty"),
                        ProblemType.UNNECESSARY_COMMENT
                    );
                }
            }
        });
    }
}
