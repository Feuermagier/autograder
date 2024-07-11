package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;

@ExecutableCheck(reportedProblems = { ProblemType.TODO_COMMENT })
public class TodoComment extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment ctComment) {
                if (ctComment.isImplicit()
                    || !ctComment.getPosition().isValidPosition()
                    || ctComment.getCommentType() != CtComment.CommentType.INLINE) {
                    return;
                }

                if (ctComment.getContent().startsWith("TODO")) {
                    addLocalProblem(
                        ctComment,
                        new LocalizedMessage("todo-comment"),
                        ProblemType.TODO_COMMENT
                    );
                }
            }
        });
    }
}
