package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonStreamUtil;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

@ExecutableCheck(reportedProblems = {ProblemType.LIST_NOT_COPIED_IN_GETTER})
public class ListGetterSetterCheck extends IntegratedCheck {
    public ListGetterSetterCheck() {
        super(new LocalizedMessage("list-getter-desc"));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtReturn<?>>() {
            @Override
            public void process(CtReturn<?> ret) {
                CtMethod<?> parentMethod = ret.getParent(CtMethod.class);
                if (parentMethod == null || !parentMethod.isPublic() || ret.getReturnedExpression() == null) {
                    return;
                }

                var returnedExpression = ret.getReturnedExpression();

                if (isMutableCollection(returnedExpression.getType())
                    && returnedExpression instanceof CtFieldRead<?> read) {
                    CtField<?> field = read.getVariable().getFieldDeclaration();
                    if (field.isPrivate() && wasMutablyAssigned(staticAnalysis, field)) {
                        addLocalProblem(ret, new LocalizedMessage("list-getter-exp"),
                            ProblemType.LIST_NOT_COPIED_IN_GETTER);
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

    private boolean wasMutablyAssigned(StaticAnalysis analysis, CtField<?> field) {
        if (field.getDefaultExpression() != null && isMutableAssignee(field.getDefaultExpression())) {
            return true;
        }

        var processor = new AbstractProcessor<CtAssignment<?, ?>>() {
            boolean mutablyAssigned = false;

            @Override
            public void process(CtAssignment<?, ?> write) {
                if (write.getAssigned() instanceof CtFieldAccess<?> ref &&
                    ref.getVariable().getFieldDeclaration().equals(field)) {
                    if (isMutableAssignee(write.getAssignment())) {
                        mutablyAssigned = true;
                    }
                }
            }
        };
        analysis.processWith(processor);
        return processor.mutablyAssigned;
    }

    private boolean isMutableAssignee(CtExpression<?> expression) {
        if (expression instanceof CtInvocation<?> invocation) {
            return !SpoonUtil.isStaticCallTo(invocation, "java.util.List", "of")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Set", "of")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Map", "of")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Collections", "unmodifiableList")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Collections", "unmodifiableSet")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Collections", "unmodifiableSortedSet")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Collections", "unmodifiableMap")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Collections", "unmodifiableSortedMap")
                && !SpoonUtil.isStaticCallTo(invocation, "java.util.Collections", "unmodifiableCollection")
                && !(SpoonStreamUtil.isStreamOperation(invocation) &&
                invocation.getExecutable().getSimpleName().equals("toList"));
            // <stream>.collect(Collectors.toList()) can be mutable or immutable

        } else {
            return expression instanceof CtConstructorCall<?>;
        }
    }
}
