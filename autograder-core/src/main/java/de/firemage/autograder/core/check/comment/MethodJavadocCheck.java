package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.JAVADOC_MISSING_PARAMETER_TAG,
    ProblemType.JAVADOC_UNKNOWN_PARAMETER_TAG, ProblemType.JAVADOC_UNEXPECTED_TAG})
public class MethodJavadocCheck extends IntegratedCheck {
    private static final List<CtJavaDocTag.TagType> VALID_TAGS = List.of(
        CtJavaDocTag.TagType.PARAM,
        CtJavaDocTag.TagType.RETURN,
        CtJavaDocTag.TagType.THROWS,
        CtJavaDocTag.TagType.EXCEPTION,
        CtJavaDocTag.TagType.SEE,
        CtJavaDocTag.TagType.UNKNOWN,
        CtJavaDocTag.TagType.DEPRECATED
    );

    public MethodJavadocCheck() {
        super(new LocalizedMessage("javadoc-method-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtMethod<?>>() {
            @Override
            public void process(CtMethod<?> method) {
                if (method.isPrivate()) {
                    return;
                }

                Optional<CtJavaDoc> javadoc = SpoonUtil.getJavadoc(method);
                if (javadoc.isEmpty()) {
                    return;
                }

                checkParameters(method, javadoc.get());
                checkValidTags(javadoc.get());
            }
        });
    }

    private boolean hasTypeParameter(String name, CtMethod<?> method) {
        return method.getFormalCtTypeParameters().stream()
            .anyMatch(param -> name.equals("<" + param.getSimpleName() + ">"));
    }

    private boolean hasParameter(String name, CtMethod<?> method) {
        return method.getParameters().stream().anyMatch(param -> param.getSimpleName().equals(name));
    }

    private void checkParameters(CtMethod<?> method, CtJavaDoc javadoc) {
        List<String> paramTags = javadoc.getTags().stream()
            .filter(tag -> tag.getType() == CtJavaDocTag.TagType.PARAM)
            .map(CtJavaDocTag::getParam)
            .map(String::trim)
            .toList();

        // Unmentioned parameters?
        for (CtParameter<?> param : method.getParameters()) {
            if (!paramTags.contains(param.getSimpleName())) {
                addLocalProblem(javadoc,
                    new LocalizedMessage(
                        "javadoc-method-exp-param-missing",
                        Map.of("param", param.getSimpleName())
                    ), ProblemType.JAVADOC_MISSING_PARAMETER_TAG);
            }
        }

        // Non-existing parameters?
        for (String tag : paramTags) {
            if (!hasParameter(tag, method) && !hasTypeParameter(tag, method)) {
                addLocalProblem(javadoc, new LocalizedMessage(
                    "javadoc-method-exp-param-unknown",
                    Map.of("param", tag)
                ), ProblemType.JAVADOC_UNKNOWN_PARAMETER_TAG);
            }
        }
    }

    private void checkValidTags(CtJavaDoc javadoc) {
        for (CtJavaDocTag tag : javadoc.getTags()) {
            if (!VALID_TAGS.contains(tag.getType())) {
                addLocalProblem(javadoc,
                    new LocalizedMessage("javadoc-method-exp-unexpected-tag", Map.of("tag", tag.getType().getName())),
                    ProblemType.JAVADOC_UNEXPECTED_TAG);
            }
        }
    }
}
