package de.firemage.autograder.core.check.comment;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.event.ReferenceReturnEvent;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtMethod;

import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.JAVADOC_INCOMPLETE_RETURN_TAG})
public class JavadocReturnNullCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtMethod<?>>() {
            @Override
            public void process(CtMethod<?> method) {
                if (method.isPrivate() || method.getType().isPrimitive()) {
                    return;
                }

                boolean returnsNull = dynamicAnalysis.findEventsForMethod(method)
                    .anyMatch(event -> event instanceof ReferenceReturnEvent refRet && refRet.returnedNull());

                if (returnsNull) {
                    Optional<CtJavaDoc> javaDoc = SpoonUtil.getJavadoc(method);
                    if (javaDoc.isEmpty()) {
                        return;
                    }
                    Optional<CtJavaDocTag> returnTag = javaDoc.get()
                        .getTags()
                        .stream()
                        .filter(tag -> tag.getType().equals(CtJavaDocTag.TagType.RETURN))
                        .findFirst();

                    if (returnTag.isPresent() && !returnTag.get().getContent()
                        .contains("null")) { // We don't care if the return tag does not exist
                        // We sadly cannot use the returnTag itself as the position because it has a "NoSourcePosition"
                        addLocalProblem(javaDoc.get(),
                            new LocalizedMessage(
                                "javadoc-return-null-exp",
                                Map.of("method", method.getSignature())
                            ),
                            ProblemType.JAVADOC_INCOMPLETE_RETURN_TAG);
                    }
                }
            }
        });
    }
}
