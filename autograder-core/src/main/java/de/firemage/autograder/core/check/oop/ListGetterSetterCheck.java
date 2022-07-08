package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

public class ListGetterSetterCheck extends IntegratedCheck {
    public ListGetterSetterCheck() {
        super("Copy mutable collections before returning them to avoid unwanted mutations by other classes");
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getSpoonModel().processWith(new AbstractProcessor<CtReturn<?>>() {
            @Override
            public void process(CtReturn<?> ret) {
                if (!ret.getParent(CtMethod.class).isPublic() || ret.getReturnedExpression() == null) {
                    return;
                }

                if (isMutableCollection(ret.getReturnedExpression().getType())
                        && ret.getReturnedExpression() instanceof CtFieldRead<?> read) {
                    CtField<?> field = read.getVariable().getFieldDeclaration();
                    if (field.isPrivate()) {
                        addLocalProblem(ret, "Copy this collection before returning it");
                    }
                }
            }
        });
    }

    private boolean isMutableCollection(CtTypeReference<?> type) {
        String name = type.getQualifiedName();
        return name.equals("java.util.List")
                || name.equals("java.util.ArrayList")
                || name.equals("java.util.LinkedList")
                || name.equals("java.util.Map")
                || name.equals("java.util.HashMap")
                || name.equals("java.util.TreeMap")
                || name.equals("java.util.Set")
                || name.equals("java.util.HashSet")
                || name.equals("java.util.LinkedHashSet")
                || name.equals("java.util.TreeSet");
        // TODO add more collections / implement an inheritance solver
    }
}
