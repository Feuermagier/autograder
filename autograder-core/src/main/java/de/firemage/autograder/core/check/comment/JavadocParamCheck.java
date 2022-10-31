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
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JavadocParamCheck extends IntegratedCheck {
    public JavadocParamCheck() {
        super(new LocalizedMessage("javadoc-param-desc"));
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

                List<String> paramTags = javadoc.get().getTags().stream()
                    .filter(tag -> tag.getType() == CtJavaDocTag.TagType.PARAM)
                    .map(CtJavaDocTag::getParam)
                    .map(String::trim)
                    .toList();

                // Unmentioned parameters?
                for (CtParameter<?> param : method.getParameters()) {
                    if (!paramTags.contains(param.getSimpleName())) {
                        addLocalProblem(javadoc.get(),
                            new LocalizedMessage(
                                "javadoc-param-exp-missing",
                                Map.of("param", param.getSimpleName())
                            ), ProblemType.JAVADOC_MISSING_PARAMETER_TAG);
                    }
                }

                // Non-existent parameters?
                for (String tag : paramTags) {
                    if (!hasParameter(tag, method) && !hasTypeParameter(tag, method)) {
                        addLocalProblem(javadoc.get(), new LocalizedMessage(
                            "javadoc-param-exp-missing",
                            Map.of("param", tag)
                        ), ProblemType.JAVADOC_UNKNOWN_PARAMETER_TAG);
                    }
                }
            }
        });
    }

    private boolean hasTypeParameter(String name, CtMethod<?> method) {
        return method.getFormalCtTypeParameters().stream().noneMatch(param -> param.getSimpleName().equals(name));
    }

    private boolean hasParameter(String name, CtMethod<?> method) {
        return method.getParameters().stream().noneMatch(param -> param.getSimpleName().equals(name));
    }
}
