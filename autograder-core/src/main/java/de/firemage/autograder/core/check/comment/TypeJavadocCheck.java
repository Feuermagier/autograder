package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class TypeJavadocCheck extends IntegratedCheck {
    private static List<CtJavaDocTag.TagType> VALID_TAGS = List.of(
        CtJavaDocTag.TagType.SEE,
        CtJavaDocTag.TagType.UNKNOWN,
        CtJavaDocTag.TagType.DEPRECATED,
        CtJavaDocTag.TagType.VERSION,
        CtJavaDocTag.TagType.AUTHOR,
        CtJavaDocTag.TagType.SINCE
    );

    private final Pattern pattern;

    public TypeJavadocCheck() {
        this("u(\\w){4}");
    }

    public TypeJavadocCheck(String regex) {
        super(new LocalizedMessage("javadoc-type-desc"));
        this.pattern = Pattern.compile(regex);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> type) {
                Optional<CtJavaDoc> javadoc = SpoonUtil.getJavadoc(type);
                if (javadoc.isEmpty()) {
                    return;
                }

                checkValidTags(javadoc.get());
                checkValidAuthor(javadoc.get());
            }
        });
    }

    private void checkValidTags(CtJavaDoc javadoc) {
        for (CtJavaDocTag tag : javadoc.getTags()) {
            if (!VALID_TAGS.contains(tag.getType())) {
                addLocalProblem(javadoc,
                    new LocalizedMessage("javadoc-type-exp-unexpected-tag", Map.of("tag", tag.getType().getName())),
                    ProblemType.JAVADOC_UNEXPECTED_TAG);
            }
        }
    }

    private void checkValidAuthor(CtJavaDoc javadoc) {
        Optional<CtJavaDocTag> authorTag = javadoc.getTags().stream()
            .filter(tag -> tag.getType() == CtJavaDocTag.TagType.AUTHOR)
            .findAny();
        
        if (authorTag.isPresent() && !this.pattern.matcher(authorTag.get().getContent().trim()).matches()) {
            addLocalProblem(javadoc, new LocalizedMessage("javadoc-type-exp-invalid-author",
                Map.of("author", authorTag.get().getContent().trim())), ProblemType.INVALID_AUTHOR_TAG);
        }
    }
}
