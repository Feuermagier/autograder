package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.CodePosition;
import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.Translatable;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.core.file.SourcePath;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.apache.commons.lang3.StringUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtComment;
import spoon.reflect.cu.SourcePosition;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@ExecutableCheck(reportedProblems = {ProblemType.COMMENTED_OUT_CODE})
public class CommentedOutCodeCheck extends IntegratedCheck {
    private static final Comparator<SourcePosition> POSITION_COMPARATOR =
        Comparator.comparingInt(SourcePosition::getSourceStart);
    private static final Translatable MESSAGE = new LocalizedMessage("commented-out-code");
    private static final Set<CtComment.CommentType> ALLOWED_COMMENT_TYPES = Set.of(
        CtComment.CommentType.BLOCK,
        CtComment.CommentType.INLINE
    );

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        Map<Path, SortedSet<SourcePosition>> files = new HashMap<>();

        staticAnalysis.processWith(new AbstractProcessor<CtComment>() {
            @Override
            public void process(CtComment comment) {
                if (!ALLOWED_COMMENT_TYPES.contains(comment.getCommentType())) {
                    return;
                }
                String content = comment.getContent().trim();

                if (StringUtils.containsAny(content, ';', '{', '}', '=')) {
                    var position = comment.getPosition();
                    files
                        .computeIfAbsent(position.getFile().toPath(), path -> new TreeSet<>(POSITION_COMPARATOR))
                        .add(position);
                }
            }
        });

        files.forEach((path, positions) -> {
            var sourcePath = getRoot().getCompilationUnit(path).path();

            var iter = positions.iterator();
            if (!iter.hasNext()) {
                return;
            }
            var running = new RunningPosition(iter.next());
            iter.forEachRemaining(position -> {
                var line = position.getLine();
                var column = position.getColumn();
                if (line == running.endLine) {
                    running.endColumn = position.getEndColumn();
                } else if (line == running.endLine + 1 && column == running.startColumn) {
                    running.endLine = position.getEndLine();
                    running.endColumn = position.getEndColumn();
                } else {
                    running.addProblem(sourcePath);
                    running.startLine = line;
                    running.startColumn = column;
                    running.endLine = position.getEndLine();
                    running.endColumn = position.getEndColumn();
                }
            });
            running.addProblem(sourcePath);
        });
    }

    private final class RunningPosition {
        int startLine;
        int endLine;
        int startColumn;
        int endColumn;

        RunningPosition(SourcePosition position) {
            startLine = position.getLine();
            endLine = position.getEndLine();
            startColumn = position.getColumn();
            endColumn = position.getEndColumn();
        }

        void addProblem(SourcePath path) {
            addLocalProblem(
                new CodePosition(getRoot(), path, startLine, endLine, startColumn, endColumn),
                MESSAGE,
                ProblemType.COMMENTED_OUT_CODE
            );
        }
    }
}
