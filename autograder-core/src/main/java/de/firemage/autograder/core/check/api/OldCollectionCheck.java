package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;

@ExecutableCheck(reportedProblems = {ProblemType.DEPRECATED_COLLECTION_USED})
public class OldCollectionCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructorCall<?>>() {
            @Override
            public void process(CtConstructorCall call) {
                String type = call.getType().getQualifiedName();
                switch (type) {
                    case "java.util.Vector" -> addLocalProblem(call, new LocalizedMessage("old-collection-exp-vector"),
                        ProblemType.DEPRECATED_COLLECTION_USED);
                    case "java.util.Hashtable" -> addLocalProblem(call,
                        new LocalizedMessage("old-collection-exp-hashtable"),
                        ProblemType.DEPRECATED_COLLECTION_USED);
                    case "java.util.Stack" -> addLocalProblem(call, new LocalizedMessage("old-collection-exp-stack"),
                        ProblemType.DEPRECATED_COLLECTION_USED);
                }
            }
        });
    }
}
