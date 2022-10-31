package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;

import java.util.regex.Pattern;

public class AuthorTagCheck extends IntegratedCheck {
    private final Pattern pattern;

    public AuthorTagCheck() {
        this("\\w+");
    }

    public AuthorTagCheck(String regex) {
        super(new LocalizedMessage("author-tag-invalid-desc"));
        this.pattern = Pattern.compile(regex);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtJavaDoc>() {
            @Override
            public void process(CtJavaDoc element) {
                for (CtJavaDocTag tag : element.getTags()) {
                    if (tag.getType() == CtJavaDocTag.TagType.AUTHOR) {
                        String content = tag.getContent().trim();
                        if (!pattern.matcher(content).matches()) {
                            addLocalProblem(element,
                                new LocalizedMessage("author-tag-invalid-exp"), ProblemType.INVALID_AUTHOR_TAG);
                        }
                    }
                }
            }
        });
    }
}
