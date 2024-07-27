package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.CoreUtil;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.ElementUtil;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Map;

@ExecutableCheck(reportedProblems = { ProblemType.UI_INPUT_SEPARATION, ProblemType.UI_OUTPUT_SEPARATION })
public class IOUISeparation extends IntegratedCheck {
    private boolean hasAccessedSystem(CtInvocation<?> ctInvocation) {
        // System.out.println(String) is a CtInvocation of the method println(String)
        // The target of the invocation is System.out, which is a CtFieldRead
        // (reads the field `out` of the class `System`)
        return ctInvocation.getTarget() instanceof CtFieldRead<?> ctFieldRead
            // access the type that contains the field
            && ctFieldRead.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            // check if the accessed field is out or err
            && List.of("out", "err").contains(ctFieldRead.getVariable().getSimpleName())
            // to narrow it down: the field out and err are static and final
            && ctFieldRead.getVariable().isStatic()
            && ctFieldRead.getVariable().isFinal()
            // check that the Attribute is accessed from the class System
            && TypeUtil.isTypeEqualTo(ctTypeAccess.getAccessedType(), System.class);
    }

    /**
     * Checks if the given invocation accesses a Scanner.
     *
     * @param ctInvocation the invocation to check
     * @return true if the invocation called a method on a Scanner, false otherwise
     */
    private boolean hasAccessedScanner(CtInvocation<?> ctInvocation) {
        return ctInvocation.getTarget() instanceof CtVariableRead<?> ctVariableRead
            && ctVariableRead.getVariable() != null // just to be sure
            && TypeUtil.isTypeEqualTo(ctVariableRead.getVariable().getType(), java.util.Scanner.class);
    }

    private static boolean isAllowedLocation(boolean requireSameClass, List<? extends CtElement> uses) {
        if (uses.isEmpty()) {
            return true;
        }

        CtElement firstElement = uses.get(0);
        CtElement commonParent = ElementUtil.findCommonParent(firstElement, uses.subList(1, uses.size()));

        CtModel ctModel = commonParent.getFactory().getModel();

        // check if they share a parent class
        if (requireSameClass) {
            return getThisOrParent(commonParent, CtType.class) != null;
        }

        CtPackage commonPackage = getThisOrParent(commonParent, CtPackage.class);

        // when two elements do not share a parent (i.e. classes are in different packages) their common parent is the root package.
        //
        // the problem is that classes can be in the root package and then have the root package as their parent.
        if (ctModel.getRootPackage().equals(commonPackage)) {
            for (CtElement ctElement : uses) {
                // parent should never be null, because the parent of all elements is the root package
                CtPackage parent = getThisOrParent(ctElement, CtPackage.class);

                if (!ctModel.getRootPackage().equals(parent)) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static <P extends CtElement> P getThisOrParent(CtElement ctElement, Class<P> parentType) {
        if (parentType.isInstance(ctElement)) {
            return (P) ctElement;
        }

        return ctElement.getParent(parentType);
    }

    private static CtElement findViolation(List<? extends CtElement> ctElements, boolean requireSameClass) {
        CtElement firstElement = ctElements.get(0);
        for (CtElement ctElement : ctElements) {
            if (!isAllowedLocation(requireSameClass, List.of(firstElement, ctElement))) {
                return ctElement;
            }
        }

        throw new IllegalStateException("No violation found");
    }

    private boolean notInMainClass(CtElement ctElement) {
        CtType<?> type = getThisOrParent(ctElement, CtType.class);

        if (type != null && type.getDeclaringType() != null) {
            type = type.getDeclaringType();
        }

        return type == null || type.getMethods().stream().noneMatch(MethodUtil::isMainMethod);
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        CtModel ctModel = staticAnalysis.getModel();

        // Check if all types are in one package. If they are, all io/ui should be in one class, otherwise in one package.
        boolean requireSameClass = true;
        CtPackage currentPackage = null;
        for (CtType<?> ctType : ctModel.getAllTypes()) {
            if (currentPackage == null) {
                currentPackage = ctType.getPackage();
            }

            if (!ctType.getPackage().equals(currentPackage)) {
                // found a class in a different package => the invocations must only be in the same package
                requireSameClass = false;
                break;
            }
        }

        List<CtInvocation> scannerUses = ctModel.filterChildren(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                new TypeFilter<>(CtType.class),
                this::notInMainClass
            )).filterChildren(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                new TypeFilter<>(CtInvocation.class),
                this::hasAccessedScanner
            ))
            .list(CtInvocation.class);

        List<CtInvocation> printUses = ctModel.filterChildren(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                new TypeFilter<>(CtType.class),
                this::notInMainClass
            )).filterChildren(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                new TypeFilter<>(CtInvocation.class),
                this::hasAccessedSystem
            ))
            .list(CtInvocation.class);


        if (!isAllowedLocation(requireSameClass, scannerUses)) {
            this.addLocalProblem(
                findViolation(scannerUses, requireSameClass),
                new LocalizedMessage(
                    "ui-input-separation",
                    Map.of(
                        "first",
                        CoreUtil.formatSourcePosition(scannerUses.get(0).getPosition())
                    )
                ),
                ProblemType.UI_INPUT_SEPARATION
            );
        }

        if (!isAllowedLocation(requireSameClass, printUses)) {
            this.addLocalProblem(
                findViolation(printUses, requireSameClass),
                new LocalizedMessage(
                    "ui-output-separation",
                    Map.of(
                        "first",
                        CoreUtil.formatSourcePosition(printUses.get(0).getPosition())
                    )
                ),
                ProblemType.UI_OUTPUT_SEPARATION
            );
        }
    }
}
