package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;

import java.util.List;

@ExecutableCheck(reportedProblems = {ProblemType.COMMENTED_OUT_CODE})
public class CommentedOutCodeCheck extends IntegratedCheck {
    private static final List<String> INLINE_CODE_INDICATORS = List.of(";", "{", "}");
    private static final List<String> BLOCK_CODE_INDICATORS = List.of(";", "{", "}", "=");

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment comment) {
                CtComment.CommentType type = comment.getCommentType();
                String content = comment.getContent().trim();

                List<String> indicators = INLINE_CODE_INDICATORS;
                if (type == CtComment.CommentType.BLOCK) {
                    indicators = BLOCK_CODE_INDICATORS;
                } else if (type != CtComment.CommentType.INLINE) {
                    return;
                }

                if (indicators.stream().anyMatch(content::contains)) {
                    addLocalProblem(
                        comment,
                        new LocalizedMessage("commented-out-code"),
                        ProblemType.COMMENTED_OUT_CODE
                    );
                }
            }
        });
    }
}
