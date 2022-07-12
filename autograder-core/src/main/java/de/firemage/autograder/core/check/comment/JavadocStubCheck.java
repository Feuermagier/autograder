package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtMethod;

public class JavadocStubCheck extends IntegratedCheck {
    private static final String DESCRIPTION =
        "Auto-generated Javadoc comments should be modified for the particular method";
    private final boolean allowGettersSettersWithEmptyDescription;

    public JavadocStubCheck(boolean allowGettersSettersWithEmptyDescription) {
        super(DESCRIPTION);
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
                    addLocalProblem(javadoc, "Javadoc has an empty description");
                }

                for (CtJavaDocTag tag : javadoc.getTags()) {
                    switch(tag.getType()) {
                        case PARAM -> {
                            if (isDefaultValueDescription(tag.getContent())) {
                                addLocalProblem(javadoc, "Stub description for parameter " + tag.getParam());
                            }
                        }
                        case RETURN -> {
                            if (isDefaultValueDescription(tag.getContent())) {
                                addLocalProblem(javadoc, "Stub description for return value");
                            }
                        }
                        case THROWS -> {
                            if (isDefaultValueDescription(tag.getContent())) {
                                addLocalProblem(javadoc, "Stub description for exception " + tag.getParam());
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
            .matches("the (bool|boolean|byte|char|short|int|integer|long|float|double|String|Object|exception|array)( value| array)?");
    }
}
