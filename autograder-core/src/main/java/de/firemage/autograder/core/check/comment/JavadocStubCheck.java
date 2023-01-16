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

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.JAVADOC_STUB_DESCRIPTION, ProblemType.JAVADOC_STUB_RETURN_TAG,
    ProblemType.JAVADOC_STUB_THROWS_TAG, ProblemType.JAVADOC_STUB_PARAMETER_TAG})
public class JavadocStubCheck extends IntegratedCheck {
    private final boolean allowGettersSettersWithEmptyDescription;

    public JavadocStubCheck(boolean allowGettersSettersWithEmptyDescription) {
        super(new LocalizedMessage("javadoc-stub-desc"));
        this.allowGettersSettersWithEmptyDescription = allowGettersSettersWithEmptyDescription;
    }

    public JavadocStubCheck() {
        this(true);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtJavaDoc>() {
            @Override
            public void process(CtJavaDoc javadoc) {
                if (allowGettersSettersWithEmptyDescription
                    && javadoc.getParent() instanceof CtMethod<?> method
                    && (SpoonUtil.isGetter(method) || SpoonUtil.isSetter(method))) {
                    // Setters and Getters are okay
                } else if (isDefaultValueDescription(javadoc.getContent())) {
                    addLocalProblem(javadoc, new LocalizedMessage("javadoc-stub-exp-desc"),
                        ProblemType.JAVADOC_STUB_DESCRIPTION);
                }

                for (CtJavaDocTag tag : javadoc.getTags()) {
                    switch (tag.getType()) {
                        case PARAM -> {
                            if (isDefaultValueDescription(tag.getContent())) {
                                addLocalProblem(javadoc,
                                    new LocalizedMessage("javadoc-stub-exp-param", Map.of("param", tag.getParam())),
                                    ProblemType.JAVADOC_STUB_PARAMETER_TAG);
                            }
                        }
                        case RETURN -> {
                            if (isDefaultValueDescription(tag.getContent())) {
                                addLocalProblem(javadoc, new LocalizedMessage("javadoc-stub-exp-return"),
                                    ProblemType.JAVADOC_STUB_RETURN_TAG);
                            }
                        }
                        case THROWS -> {
                            if (isDefaultValueDescription(tag.getContent())) {
                                addLocalProblem(javadoc,
                                    new LocalizedMessage("javadoc-stub-exp-throws", Map.of("exp", tag.getParam())),
                                    ProblemType.JAVADOC_STUB_THROWS_TAG);
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        });
    }

    private boolean isDefaultValueDescription(String description) {
        description = description.toLowerCase().replace(".", "").replace(",", "");
        return description.isBlank()
            || description.equals("parameter")
            || description.equals("param")
            || description.equals("return value")
            || description.equals("todo")
            || description.equals("null")
            || description.equals("description")
            || description.equals("beschreibung")
            || description.trim()
            .matches(
                "the (bool|boolean|byte|char|short|int|integer|long|float|double|string|object|exception|array)( value| array)?");
    }
}
