package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtElement;

import java.util.Map;

@ExecutableCheck(reportedProblems = {ProblemType.DEPRECATED_COLLECTION_USED})
public class OldCollectionCheck extends IntegratedCheck {
    private void reportProblem(CtElement ctElement, String original, String suggestion) {
        this.addLocalProblem(
            ctElement,
            new LocalizedMessage("suggest-replacement", Map.of("original", original, "suggestion", suggestion)),
            ProblemType.DEPRECATED_COLLECTION_USED
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructorCall<?>>() {
            @Override
            public void process(CtConstructorCall ctConstructorCall) {
                switch (ctConstructorCall.getType().getQualifiedName()) {
                    case "java.util.Vector" -> reportProblem(ctConstructorCall, "Vector", "ArrayList");
                    case "java.util.Hashtable" -> reportProblem(ctConstructorCall, "Hashtable", "HashMap");
                    case "java.util.Stack" -> reportProblem(ctConstructorCall, "Stack", "Dequeue");
                }
            }
        });
    }
}
