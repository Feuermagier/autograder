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
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FieldJavadocCheck extends IntegratedCheck {
    private static List<CtJavaDocTag.TagType> VALID_TAGS = List.of(
        CtJavaDocTag.TagType.SEE,
        CtJavaDocTag.TagType.UNKNOWN,
        CtJavaDocTag.TagType.DEPRECATED
    );

    public FieldJavadocCheck() {
        super(new LocalizedMessage("javadoc-field-check"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (field.isPrivate()) {
                    return;
                }

                Optional<CtJavaDoc> javadoc = SpoonUtil.getJavadoc(field);
                if (javadoc.isEmpty()) {
                    return;
                }

                checkValidTags(javadoc.get());
            }
        });
    }

    private void checkValidTags(CtJavaDoc javadoc) {
        for (CtJavaDocTag tag : javadoc.getTags()) {
            if (!VALID_TAGS.contains(tag.getType())) {
                addLocalProblem(javadoc,
                    new LocalizedMessage("javadoc-field-exp-unexpected-tag", Map.of("tag", tag.getType().getName())),
                    ProblemType.JAVADOC_UNEXPECTED_TAG);
            }
        }
    }
}
