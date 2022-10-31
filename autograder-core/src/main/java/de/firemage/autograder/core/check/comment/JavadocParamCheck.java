package de.firemage.autograder.core.check.comment;

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
import java.util.Optional;

public class JavadocParamCheck extends IntegratedCheck {
    public JavadocParamCheck() {
        super("Javadoc comments for methods must mention all declared parameters.");
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
                            String.format("The parameter '%s' is not mentioned in the Javadoc comment",
                                param.getSimpleName()), ProblemType.JAVADOC_MISSING_PARAMETER_TAG);
                    }
                }

                // Non-existent parameters?
                for (String tag : paramTags) {
                    if (!hasParameter(tag, method) && !hasTypeParameter(tag, method)) {
                        addLocalProblem(javadoc.get(), String.format(
                            "Javadoc mentions parameter '%s', but there is no such parameter in the method declaration",
                            tag), ProblemType.JAVADOC_UNKNOWN_PARAMETER_TAG);
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
