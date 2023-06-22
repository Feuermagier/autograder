package de.firemage.autograder.core.check.structure;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtIntersectionTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExecutableCheck(reportedProblems = {ProblemType.UI_OUTPUT_SEPARATION})
public class StructuralIOUISeparationCheck extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        Set<CtType<?>> directUITypes = this.findDirectUITypes(staticAnalysis);
        Set<CtType<?>> uiTypes = this.findIndirectUITypes(directUITypes, staticAnalysis);

        CtPackage commonUIPackage = SpoonUtil.findCommonPackage(uiTypes);

        if (uiTypes.size() / (double) staticAnalysis.getAllTypes().size() > 0.2) {
            this.addLocalProblem(staticAnalysis.getCodeModel().findMain(), new LocalizedMessage("output-sep"), ProblemType.UI_OUTPUT_SEPARATION);
        }
    }

    private Set<CtType<?>> findDirectUITypes(StaticAnalysis staticAnalysis) {
        Set<CtType<?>> uiTypes = new HashSet<>();

        staticAnalysis.processWith(new AbstractProcessor<CtInvocation<?>>() {
            @Override
            public void process(CtInvocation<?> invocation) {
                if (hasAccessedOutErrStream(invocation)) {
                    uiTypes.add(invocation.getParent(CtType.class));
                }
            }
        });

        // Main method is also UI
        uiTypes.add(staticAnalysis.getCodeModel().findMain().getDeclaringType());

        return uiTypes;
    }

    private Set<CtType<?>> findIndirectUITypes(Set<CtType<?>> directUITypes, StaticAnalysis staticAnalysis) {
        Set<CtType<?>> uiTypes = new HashSet<>(directUITypes);
        Collection<CtType<?>> allTypes = staticAnalysis.getAllTypes();

        boolean newTypeAdded = true;
        while (newTypeAdded) {
            Set<CtType<?>> newTypes = new HashSet<>();
            for (CtType<?> type : allTypes) {
                if (uiTypes.contains(type)) {
                    continue;
                }

                for (CtTypeReference<?> ref : type.getReferencedTypes()) {
                    if (referencesUIType(ref, uiTypes)) {
                        newTypes.add(type);
                        break;
                    }
                }
            }
            uiTypes.addAll(newTypes);
            newTypeAdded = !newTypes.isEmpty();
        }

        return uiTypes;
    }

    private boolean hasAccessedOutErrStream(CtInvocation<?> ctInvocation) {
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
                && ctInvocation.getFactory().Type().createReference(System.class).equals(ctTypeAccess.getAccessedType());
    }

    private static boolean referencesUIType(CtTypeReference<?> typeRef, Set<CtType<?>> uiTypes) {
        // Type itself
        if (uiTypes.contains(typeRef.getTypeDeclaration())) {
            return true;
        }

        // Type parameters
        for (CtTypeReference<?> genericRef : typeRef.getActualTypeArguments()) {
            if (referencesUIType(genericRef, uiTypes)) {
                return true;
            }
        }

        // Intersection type parts
        if (typeRef instanceof CtIntersectionTypeReference<?> intersectionTypeRef) {
            for (CtTypeReference<?> ref : intersectionTypeRef.getActualTypeArguments()) {
                if (referencesUIType(ref, uiTypes)) {
                    return true;
                }
            }
        }

        // Wildcard bounding types
        if (typeRef instanceof CtWildcardReference wildcard) {
            if (wildcard.getBoundingType() != null) {
                if (referencesUIType(wildcard.getBoundingType(), uiTypes)) {
                    return true;
                }
            }
        }

        return false;
    }
}
