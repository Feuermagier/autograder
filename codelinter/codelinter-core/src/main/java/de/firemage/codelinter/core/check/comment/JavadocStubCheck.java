package de.firemage.codelinter.core.check.comment;

import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.integrated.SpoonUtil;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
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
                } else if (javadoc.getContent().isBlank()) {
                    addLocalProblem(javadoc, "Javadoc has an empty description");
                }

                for (CtJavaDocTag tag : javadoc.getTags()) {
                    switch(tag.getType()) {
                        case PARAM -> {
                            if (isDefaultValueDescription(tag)) {
                                addLocalProblem(javadoc, "Stub description for parameter " + tag.getParam());
                            }
                        }
                        case RETURN -> {
                            if (isDefaultValueDescription(tag)) {
                                addLocalProblem(javadoc, "Stub description for return value");
                            }
                        }
                        case THROWS -> {
                            if (isDefaultValueDescription(tag)) {
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

    private boolean isDefaultValueDescription(CtJavaDocTag tag) {
        return tag.getContent().isBlank()
            || tag.getContent().equals("parameter")
            || tag.getContent().equals("param")
            || tag.getContent().equals("return value")
            || tag.getContent().equals("TODO")
            || tag.getContent().equals("null")
            || tag.getContent().trim()
            .matches("the (bool|boolean|byte|char|short|int|integer|long|float|double|String|Object|exception|array)( value| array)?");
    }
}
