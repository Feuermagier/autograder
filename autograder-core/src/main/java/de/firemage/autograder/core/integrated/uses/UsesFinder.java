package de.firemage.autograder.core.integrated.uses;

import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.stream.CtElementStream;
import spoon.reflect.CtModel;
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
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

/**
 * Provides usage-relationships between code elements in the code model.
 * Uses are tracked for variables of all kinds, type parameters, executables, and types.
 * The graph is built once at construction time with a single pass over the model for all elements,
 * so that all subsequent queries are fast.
 */
public class UsesFinder {
    private static final String METADATA_KEY = "autograder_uses";

    private final UsesScanner scanner;

    private UsesFinder(CtModel model) {
        this.scanner = new UsesScanner();
        model.getRootPackage().accept(this.scanner);
    }

    public static void buildFor(CtModel model) {
        UsesFinder uses = new UsesFinder(model);
        model.getRootPackage().putMetadata(METADATA_KEY, uses);
    }

    public static UsesFinder getFor(CtElement element) {
        var uses = (UsesFinder) SpoonUtil.getRootPackage(element).getMetadata(METADATA_KEY);
        if (uses == null) {
            throw new IllegalArgumentException("No uses information available for this model");
        }
        return uses;
    }

    /**
     * Find all uses of the given element within the model.
     * This is an (untyped) alternative to the more specific methods (variableUses, typeParameterUses, executableUses, typeUses).
     * @param element
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static CtElementStream<CtElement> getAllUses(CtNamedElement element) {
        if (element instanceof CtVariable<?> variable) {
            return UsesFinder.variableUses(variable).asUntypedStream();
        } else if (element instanceof CtTypeParameter typeParameter) {
            return UsesFinder.typeParameterUses(typeParameter).asUntypedStream();
        } else if (element instanceof CtExecutable executable) {
            return UsesFinder.executableUses(executable).asUntypedStream();
        } else if (element instanceof CtType type) {
            return UsesFinder.typeUses(type).asUntypedStream();
        } else {
            throw new IllegalArgumentException("Unsupported element: " + element.getClass().getName());
        }
    }

    public static CtElementStream<CtVariableAccess<?>> variableUses(CtVariable<?> variable) {
        return CtElementStream.of(UsesFinder.getFor(variable).scanner.variableUses.getOrDefault(variable, List.of())).assumeElementType();
    }

    public static CtElementStream<CtTypeParameterReference> typeParameterUses(CtTypeParameter typeParameter) {
        return CtElementStream.of(UsesFinder.getFor(typeParameter).scanner.typeParameterUses.getOrDefault(typeParameter, List.of()));
    }

    public static CtElementStream<CtElement> executableUses(CtExecutable<?> executable) {
        return CtElementStream.of(UsesFinder.getFor(executable).scanner.executableUses.getOrDefault(executable, List.of()));
    }

    public static CtElementStream<CtTypeReference<?>> typeUses(CtType<?> type) {
        return CtElementStream.of(UsesFinder.getFor(type).scanner.typeUses.getOrDefault(type, List.of())).assumeElementType();
    }

    /**
     * The scanner searches for uses of supported code elements in a single pass over the entire model.
     * Since inserting into the hash map requires (amortized) constant time, usage search runs in O(n) time.
     */
    @SuppressWarnings("rawtypes")
    private static class UsesScanner extends CtScanner {
        // The IdentityHashMaps are very important here, since
        // E.g. CtVariable's equals method considers locals with the same name to be equal
        public final IdentityHashMap<CtVariable, List<CtVariableAccess>> variableUses = new IdentityHashMap<>();
        public final IdentityHashMap<CtTypeParameter, List<CtTypeParameterReference>> typeParameterUses = new IdentityHashMap<>();
        public final IdentityHashMap<CtExecutable, List<CtElement>> executableUses = new IdentityHashMap<>();
        public final IdentityHashMap<CtType, List<CtTypeReference>> typeUses = new IdentityHashMap<>();

        // Caches the current instanceof pattern variables, since Spoon doesn't track them yet
        // We are conservative: A pattern introduces a variable until the end of the current block
        // (in reality, the scope is based on 'normal completion', see JLS, but the rules are way too complex for us)
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
            this.recordExecutableReference(invocation.getExecutable(), invocation);
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
            this.recordExecutableReference(expression.getExecutable(), expression);
            super.visitCtExecutableReferenceExpression(expression);
        }

        @Override
        public <T> void visitCtConstructorCall(CtConstructorCall<T> ctConstructorCall) {
            this.recordExecutableReference(ctConstructorCall.getExecutable(), ctConstructorCall);
            super.visitCtConstructorCall(ctConstructorCall);
        }

        @Override
        public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
            this.recordTypeReference(reference);
            super.visitCtTypeReference(reference);
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
                } else if (variableAccess.getVariable() instanceof CtFieldReference<?> fieldReference) {
                    // We can use the shadow model
                    variable = fieldReference.getFieldDeclaration();
                }
            }

            if (variable != null) {
                var accesses = this.variableUses.computeIfAbsent(variable, k -> new ArrayList<>());
                accesses.add(variableAccess);
            }
        }

        private void recordTypeParameterReference(CtTypeParameterReference reference) {
            CtTypeParameter parameter = reference.getDeclaration();
            if (parameter != null) {
                var uses = this.typeParameterUses.computeIfAbsent(parameter, k -> new ArrayList<>());
                uses.add(reference);
            }
        }

        private void recordExecutableReference(CtExecutableReference reference, CtElement referencingElement) {
            var executable = reference.getExecutableDeclaration();
            if (executable != null) {
                var uses = this.executableUses.computeIfAbsent(executable, k -> new ArrayList<>());
                uses.add(referencingElement);
            }
        }

        private void recordTypeReference(CtTypeReference reference) {
            if (reference instanceof CtArrayTypeReference<?> arrayType) {
                reference = arrayType.getArrayType();
            }

            var type = reference.getTypeDeclaration();
            if (type != null) {
                var uses = this.typeUses.computeIfAbsent(type, k -> new ArrayList<>());
                uses.add(reference);
            }
        }
    }
}
