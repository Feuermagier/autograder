package de.firemage.codelinter.core.check.javadoc;

import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import de.firemage.codelinter.event.ReferenceReturnEvent;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtJavaDocTag;
import spoon.reflect.declaration.CtMethod;
import java.util.Optional;

public class JavadocReturnNullCheck extends IntegratedCheck {
    private static final String DESCRIPTION = "Methods must document in the @return-annotation if they may return null";

    public JavadocReturnNullCheck() {
        super(DESCRIPTION);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtMethod<?>>() {
            @Override
            public void process(CtMethod<?> method) {
                boolean returnsNull = dynamicAnalysis.findEventsForMethod(method)
                        .anyMatch(event -> event instanceof ReferenceReturnEvent refRet && refRet.returnedNull());

                if (returnsNull) {
                    if (method.getComments().isEmpty() || !(method.getComments().get(0) instanceof CtJavaDoc)) {
                        // TODO lookup inherited javadoc
                        return;
                    }

                    CtJavaDoc javaDoc = method.getComments().get(0).asJavaDoc();
                    Optional<CtJavaDocTag> returnTag = javaDoc
                            .getTags()
                            .stream()
                            .filter(tag -> tag.getType().equals(CtJavaDocTag.TagType.RETURN))
                            .findFirst();

                    if (returnTag.isEmpty()) {
                        addLocalProblem(javaDoc, "The method may return null but the Javadoc doesn't mention it");
                    } else if (!returnTag.get().getContent().contains("null")) {
                        // We sadly cannot use the returnTag itself as the position because it has a "NoSourcePosition"
                        addLocalProblem(javaDoc, "The method may return null but the @return tag doesn't mention it");
                    }
                }
            }
        });
    }
}
