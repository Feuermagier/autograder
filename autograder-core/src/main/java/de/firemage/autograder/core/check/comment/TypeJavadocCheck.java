package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.ElementUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@ExecutableCheck(reportedProblems = {ProblemType.JAVADOC_UNEXPECTED_TAG, ProblemType.INVALID_AUTHOR_TAG})
public class TypeJavadocCheck extends IntegratedCheck {
    private static final List<CtJavaDocTag.TagType> VALID_TAGS = List.of(
        CtJavaDocTag.TagType.SEE,
        CtJavaDocTag.TagType.UNKNOWN,
        CtJavaDocTag.TagType.DEPRECATED,
        CtJavaDocTag.TagType.VERSION,
        CtJavaDocTag.TagType.AUTHOR,
        CtJavaDocTag.TagType.SINCE,
        // used for documenting generic types @param <T> description of T
        CtJavaDocTag.TagType.PARAM
    );

    private static final Set<String> ALLOWED_AUTHORS = Set.of("Programmieren-Team");

    private final Pattern pattern;

    public TypeJavadocCheck() {
        this("u(\\w){4}");
    }

    public TypeJavadocCheck(String regex) {
        super();
        this.pattern = Pattern.compile(regex);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> type) {
                Optional<CtJavaDoc> javadoc = ElementUtil.getJavadoc(type);
                if (javadoc.isEmpty()) {
                    return;
                }

                checkValidTags(javadoc.get(), type);
                checkValidAuthor(javadoc.get());
            }
        });
    }

    private void checkValidTags(CtJavaDoc javadoc, CtType<?> type) {
        for (CtJavaDocTag tag : javadoc.getTags()) {
            if (!VALID_TAGS.contains(tag.getType())) {
                if (tag.getType() == CtJavaDocTag.TagType.PARAM && type instanceof CtRecord) {
                    continue;
                }

                addLocalProblem(javadoc,
                    new LocalizedMessage("javadoc-unexpected-tag", Map.of("tag", tag.getType().getName())),
                    ProblemType.JAVADOC_UNEXPECTED_TAG);
            }
        }
    }

    private void checkValidAuthor(CtJavaDoc javadoc) {
        List<CtJavaDocTag> authorTags = javadoc.getTags().stream()
            .filter(tag -> tag.getType() == CtJavaDocTag.TagType.AUTHOR)
            .toList();

        // check that there is at least one valid author tag.
        // Why not check that all are valid? Someone might use
        // an old solution and keep the original authors in the javadoc.
        //
        // Those old solutions are never valid, so this would cause
        // false-positives.
        Collection<String> invalidAuthors = new ArrayList<>();
        boolean hasValidAuthor = false;
        for (CtJavaDocTag authorTag : authorTags) {
            String author = authorTag.getContent().trim();

            if (this.pattern.matcher(author).matches() || ALLOWED_AUTHORS.contains(author)) {
                hasValidAuthor = true;
            } else {
                invalidAuthors.add(author);
            }
        }

        if (!hasValidAuthor) {
            this.addLocalProblem(
                javadoc,
                new LocalizedMessage(
                    "javadoc-type-exp-invalid-author",
                    Map.of("authors", String.join(", ", invalidAuthors))
                ),
                ProblemType.INVALID_AUTHOR_TAG
            );
        }
    }
}
