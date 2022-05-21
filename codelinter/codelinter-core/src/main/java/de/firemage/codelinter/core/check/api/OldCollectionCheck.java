package de.firemage.codelinter.core.check.api;

import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.integrated.IntegratedCheck;
import de.firemage.codelinter.core.integrated.StaticAnalysis;
import de.firemage.codelinter.core.pmd.PMDCheck;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtElement;
import java.util.List;

public class OldCollectionCheck extends IntegratedCheck {
    public static final String DESCRIPTION = "Don't use Java's old collection types (Vector -> ArrayList, Stack -> Deque, Hashtable -> HashMap)";

    public OldCollectionCheck() {
        super(DESCRIPTION);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtConstructorCall<?>>() {
            @Override
            public void process(CtConstructorCall call) {
                String type = call.getType().getQualifiedName();
                switch (type) {
                    case "java.util.Vector" -> addLocalProblem(call, "Used java.util.Vector");
                    case "java.util.Hashtable" -> addLocalProblem(call, "Used java.util.Hashtable");
                    case "java.util.Stack" -> addLocalProblem(call, "Used java.util.Stack");
                }
            }
        });
    }
}
