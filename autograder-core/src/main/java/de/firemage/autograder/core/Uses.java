package de.firemage.autograder.core;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Uses {
    private final Map<CtVariable, List<CtVariableAccess>> variableAccesses;
    // private final Map<CtExecutableReference, List<CtExecutableReference>> executableAccesses;
    // private final Map<CtTypeParameter, List<CtTypeParameterReference>> typeParameterAccesses;

    public Uses(CtModel model) {
        this.variableAccesses = new HashMap<>();

        model.processWith(new AbstractProcessor<CtVariableAccess<?>>() {
            @Override
            public void process(CtVariableAccess access) {
                var variable = access.getVariable().getDeclaration();
                if (variable != null) {
                    var accesses = variableAccesses.computeIfAbsent(variable, k -> new ArrayList<>());
                    accesses.add(access);
                }
            }
        });

        for (var v : this.variableAccesses.keySet()) {
            System.out.println(v.getPosition().getLine() + " " + v.getClass().getSimpleName() + " " + v.getSimpleName() + " " + this.variableAccesses.get(v).size());
        }
    }

    public List<CtVariableAccess> findAllUses(CtVariable<?> variable) {
        return Collections.unmodifiableList(this.variableAccesses.get(variable));
    }

    public boolean hasAnyUses(CtVariable<?> variable, Predicate<? super CtVariableAccess> filter) {
        var uses = this.variableAccesses.get(variable);
        if (uses == null || uses.isEmpty()) {
            return false;
        } else {
            return uses.stream().anyMatch(filter);
        }
    }

    public boolean isConsideredUnused(CtNamedElement element, boolean hasMainMethod) {
        // ignore exception constructors and params in those constructors
        var parentConstructor = SpoonUtil.getParentOrSelf(element, CtConstructor.class);
        if (parentConstructor != null && SpoonUtil.isSubtypeOf(parentConstructor.getType(), java.lang.Throwable.class)) {
            return false;
        }

        // Special cases for public API if we have no main method:
        if (!hasMainMethod) {
            // ignore unused parameters of non-private methods
            if (element instanceof CtParameter<?> && element.getParent() instanceof CtTypeMember typeMember && !typeMember.getDeclaringType().isPrivate()) {
                return false;
            }

            // ignore unused public type members (i.e. fields, methods, ...)
            if (element instanceof CtTypeMember typeMember && !typeMember.isPrivate() && !typeMember.getDeclaringType().isPrivate()) {
                return false;
            }
        }

        if (element instanceof CtVariable<?> variable) {
            if (element instanceof CtParameter<?> && element.getParent() instanceof CtTypeMember typeMember && !typeMember.getDeclaringType().isPrivate()) {
                return false;
            }
            return this.hasAnyUses(variable, access -> true);
        } else {
            throw new IllegalArgumentException("Unsupported element: " + element.getClass().getName());
        }
    }
}
