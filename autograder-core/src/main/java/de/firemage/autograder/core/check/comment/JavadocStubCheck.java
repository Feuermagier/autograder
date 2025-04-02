package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;

@ExecutableCheck(reportedProblems = {
    ProblemType.JAVADOC_STUB_DESCRIPTION, ProblemType.JAVADOC_STUB_RETURN_TAG,
    ProblemType.JAVADOC_STUB_THROWS_TAG, ProblemType.JAVADOC_STUB_PARAMETER_TAG
})
public class JavadocStubCheck extends IntegratedCheck {
    private static final boolean ALLOW_GETTER_SETTER_WITH_EMPTY_DESCRIPTION = true;

    private static String formatTag(CtJavaDocTag tag) {
        String result = tag.getType().toString();

        if (tag.getParam() != null && !tag.getParam().isEmpty()) {
            result += " " + tag.getParam();
        }

        if (tag.getContent() != null && !tag.getContent().isEmpty()) {
            result += " " + tag.getContent();
        }

        return result;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtJavaDoc>() {
            @Override
            public void process(CtJavaDoc javadoc) {
                // skip methods that were overridden
                if (MethodUtil.isInOverridingMethod(javadoc)) {
                    return;
                }

                if (!(ALLOW_GETTER_SETTER_WITH_EMPTY_DESCRIPTION
                    && javadoc.getParent() instanceof CtMethod<?> method
                    && (MethodUtil.isGetter(method) || MethodUtil.isSetter(method)))
                    && isDefaultValueDescription(javadoc.getContent())) {
                    addLocalProblem(javadoc, new LocalizedMessage("javadoc-stub-description"), ProblemType.JAVADOC_STUB_DESCRIPTION);
                }

                for (CtJavaDocTag tag : javadoc.getTags()) {
                    if (tag.getContent() == null || !isDefaultValueDescription(tag.getContent())) {
                        continue;
                    }

                    ProblemType problemType = switch (tag.getType()) {
                        case PARAM -> ProblemType.JAVADOC_STUB_PARAMETER_TAG;
                        case RETURN -> ProblemType.JAVADOC_STUB_RETURN_TAG;
                        case THROWS -> ProblemType.JAVADOC_STUB_THROWS_TAG;
                        default -> null;
                    };

                    if (problemType != null) {
                        addLocalProblem(
                            javadoc,
                            new LocalizedMessage(
                                "javadoc-stub-tag",
                                Map.of(
                                    "tag", formatTag(tag)
                                )
                            ),
                            problemType
                        );
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
