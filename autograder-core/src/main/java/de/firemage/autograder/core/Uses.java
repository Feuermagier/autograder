package de.firemage.autograder.core;

import de.firemage.autograder.core.integrated.MethodHierarchy;
import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExecutableReferenceExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypePattern;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class Uses {
    private final MethodHierarchy methodHierarchy;
    private final UsesScanner scanner;

    public Uses(CtModel model, MethodHierarchy methodHierarchy) {
        this.methodHierarchy = methodHierarchy;
        this.scanner = new UsesScanner();
        model.getRootPackage().accept(this.scanner);
    }

    public boolean hasAnyUses(CtVariable<?> variable, Predicate<? super CtVariableAccess> filter) {
        return this.hasAnyUses(this.scanner.variableUses.get(variable), filter);
    }

    public boolean hasAnyUses(CtTypeParameter typeParameter, Predicate<? super CtTypeParameterReference> filter) {
        return this.hasAnyUses(this.scanner.typeParameterUses.get(typeParameter), filter);
    }

    public boolean hasAnyUses(CtExecutable executable, Predicate<? super CtElement> filter) {
        return this.hasAnyUses(this.scanner.executableUses.get(executable), filter);
    }

    private <T> boolean hasAnyUses(List<T> uses, Predicate<? super T> filter) {
        if (uses == null || uses.isEmpty()) {
            return false;
        } else {
            return uses.stream().anyMatch(filter);
        }
    }

    /**
     * This method implements a number of special cases for elements that we allow to be unused,
     * e.g. methods of a public API (when no main method is present), or parameters mandated by Java's API.
     *
     * @param element
     * @param hasMainMethod
     * @return
     */
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
            if (this.hasAnyUses(variable, access -> true)) {
                return false;
            } else if (variable instanceof CtParameter<?> parameter && parameter.getParent() instanceof CtMethod<?> method) {
                // For method parameters, also look in overriding methods
                int parameterIndex = SpoonUtil.getParameterIndex(parameter, method);
                return this.methodHierarchy
                        .getAllOverridingMethods(method)
                        .allMatch(m -> this.isConsideredUnused(m.getExecutable().getParameters().get(parameterIndex), hasMainMethod));
            }
            return true;
        } else if (element instanceof CtTypeParameter typeParameter) {
            return !this.hasAnyUses(typeParameter, reference -> true);
        } else if (element instanceof CtExecutable<?> executable) {
            // Ignore recursive calls
            if (this.hasAnyUses(executable, reference -> reference.getParent(CtMethod.class) != executable)) {
                return false;
            } else if (executable instanceof CtMethod<?> method) {
                // For methods, also look for used overriding methods
                return this.methodHierarchy
                        .getAllOverridingMethods(method)
                        .allMatch(m -> this.isConsideredUnused(m.getExecutable(), hasMainMethod));
            }
            return true;
        } else {
            throw new IllegalArgumentException("Unsupported element: " + element.getClass().getName());
        }
    }

    /**
     * The scanner searches for uses of supported code elements in a single pass over the entire model.
     * Since inserting into the hash map requires (amortized) constant time, usage search runs in O(n) time.
     */
    static class UsesScanner extends CtScanner {
        // The IdentityHashMaps are very important here, since
        // E.g. CtVariable's equals method considers locals with the same name to be equal
        public final IdentityHashMap<CtVariable, List<CtVariableAccess>> variableUses = new IdentityHashMap<>();
        public final IdentityHashMap<CtTypeParameter, List<CtTypeParameterReference>> typeParameterUses = new IdentityHashMap<>();
        public final IdentityHashMap<CtExecutable, List<CtElement>> executableUses = new IdentityHashMap<>();

        // Caches the current instanceof pattern variables, since Spoon doesn't track them yet
        // We are conservative: A pattern introduces a variable until the end of the current block
        // (in reality, the scope is based on 'normal completion', see JLS, but the rules look way too complex for us)
        private final Stack<HashMap<String, CtVariable>> instanceofPatternVariables = new Stack<>();

        @Override
        public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
            this.recordVariableAccess(variableRead);
            super.visitCtVariableRead(variableRead);
        }

        @Override
        public <T> void visitCtVariableWrite(CtVariableWrite<T> variableWrite) {
            this.recordVariableAccess(variableWrite);
            super.visitCtVariableWrite(variableWrite);
        }

        @Override
        public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
            this.recordVariableAccess(fieldRead);
            super.visitCtFieldRead(fieldRead);
        }

        @Override
        public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
            this.recordVariableAccess(fieldWrite);
            super.visitCtFieldWrite(fieldWrite);
        }

        @Override
        public <T> void visitCtInvocation(CtInvocation<T> invocation) {
            if (invocation.getTarget() instanceof CtVariableAccess reference) {
                this.recordVariableAccess(reference);
            }
            this.recordExecutableReference(invocation.getExecutable());
            super.visitCtInvocation(invocation);
        }

        @Override
        public void visitCtTypePattern(CtTypePattern pattern) {
            var variable = pattern.getVariable();
            this.instanceofPatternVariables.peek().put(variable.getSimpleName(), variable);
            super.visitCtTypePattern(pattern);
        }

        @Override
        public void visitCtTypeParameterReference(CtTypeParameterReference reference) {
            this.recordTypeParameterReference(reference);
            super.visitCtTypeParameterReference(reference);
        }

        @Override
        public <T, E extends CtExpression<?>> void visitCtExecutableReferenceExpression(CtExecutableReferenceExpression<T, E> expression) {
            this.recordExecutableReference(expression.getExecutable());
            super.visitCtExecutableReferenceExpression(expression);
        }

        @Override
        public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
            this.recordExecutableReference(ctConstructorCall.getExecutable());
            super.visitCtConstructorCall(ctConstructorCall);
        }

        @Override
        public <R> void visitCtBlock(CtBlock<R> block) {
            this.instanceofPatternVariables.push(new HashMap<>());
            super.visitCtBlock(block);
            this.instanceofPatternVariables.pop();
        }

        private void recordVariableAccess(CtVariableAccess<?> variableAccess) {
            var variable = variableAccess.getVariable().getDeclaration();

            if (variable == null) {
                // Try a few other ways to get the declaration
                if (variableAccess.getVariable() instanceof CtLocalVariableReference) {
                    // Look for an instanceof pattern variable which is in scope
                    for (var scope : this.instanceofPatternVariables) {
                        variable = scope.get(variableAccess.getVariable().getSimpleName());
                        if (variable != null) {
                            break;
                        }
                    }
                }
            }

            if (variable != null) {
                var accesses = this.variableUses.computeIfAbsent(variable, k -> new ArrayList<>());
                accesses.add(variableAccess);
            }
        }

        private void recordTypeParameterReference(CtTypeParameterReference reference) {
            var type = reference.getDeclaration();
            if (type != null) {
                var uses = this.typeParameterUses.computeIfAbsent(type, k -> new ArrayList<>());
                uses.add(reference);
            }
        }

        private void recordExecutableReference(CtExecutableReference reference) {
            var executable = reference.getDeclaration();
            if (executable != null) {
                var uses = this.executableUses.computeIfAbsent(executable, k -> new ArrayList<>());
                uses.add(reference);
            }
        }
    }
}
