package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;

@ExecutableCheck(reportedProblems = {ProblemType.COMMENTED_OUT_CODE})
public class CommentedOutCodeCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment comment) {
                var type = comment.getCommentType();
                String content = comment.getContent().trim();

                if (type == CtComment.CommentType.INLINE) {
                    if (content.endsWith(";") || content.equals("{") || content.equals("}")) {
                        addLocalProblem(comment, new LocalizedMessage("commented-out-code-exp"),
                            ProblemType.COMMENTED_OUT_CODE);
                    }
                } else if (type == CtComment.CommentType.BLOCK) {
                    if (content.contains(";") || content.contains("=") || content.contains("{") ||
                        content.contains("}")) {
                        addLocalProblem(comment, new LocalizedMessage("commented-out-code-exp"),
                            ProblemType.COMMENTED_OUT_CODE);
                    }
                }
            }
        });
    }
}
