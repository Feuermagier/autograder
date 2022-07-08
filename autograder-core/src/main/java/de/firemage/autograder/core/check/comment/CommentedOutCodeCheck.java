package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;

public class CommentedOutCodeCheck extends IntegratedCheck {
    private static final String DESCRIPTION = "Unused code should be removed and not commented out";

    public CommentedOutCodeCheck() {
        super(DESCRIPTION);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getSpoonModel().processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment comment) {
                var type = comment.getCommentType();
                String content = comment.getContent().trim();

                if (type == CtComment.CommentType.INLINE) {
                    if (content.endsWith(";") || content.equals("{") || content.equals("}")) {
                        addLocalProblem(comment, "This commented out code should be removed");
                    }
                } else if (type == CtComment.CommentType.BLOCK) {
                    if (content.contains(";") || content.contains("=") || content.contains("{") ||
                        content.contains("}")) {
                        addLocalProblem(comment, "This commented out code should be removed");
                    }
                }
            }
        });
    }
}
