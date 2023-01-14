package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.code.CtRHSReceiver;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.code.CtLiteralImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

class Scope {
    private final List<Map<CtVariableReference<?>, CtExpression<?>>> variables;

    Scope() {
        this.variables = new ArrayList<>();
        this.variables.add(new HashMap<>());
    }

    /**
     * Registers the variable in the current scope having the specified value.
     *
     * @param variable the variable to register
     * @param value    the value it has
     */
    void register(CtVariableReference<?> variable, CtExpression<?> value) {
        this.variables.get(this.variables.size() - 1).put(variable, value);
    }

    /**
     * Removes the variable from the scope.
     *
     * @param variable the variable to remove.
     */
    void remove(CtVariableReference<?> variable) {
        this.variables.get(this.variables.size() - 1).remove(variable);
    }

    /**
     * Returns the value of the variable or null.
     *
     * @param variable the variable of which one should get the value
     *
     * @return the value or null if it is unknown.
     */
    CtExpression<?> get(CtVariableReference<?> variable) {
        List<Map<CtVariableReference<?>, CtExpression<?>>> reversedVars = new ArrayList<>(this.variables);
        Collections.reverse(reversedVars);

        for (var scope : reversedVars) {
            if (scope.containsKey(variable)) {
                return scope.get(variable);
            }
        }

        return null;
    }

    /**
     * Adds a new (empty) scope to the stack.
     */
    void push() {
        this.variables.add(new HashMap<>());
    }

    /**
     * Removes the last scope from the stack.
     */
    void pop() {
        this.variables.remove(this.variables.size() - 1);
        if (this.variables.isEmpty()) {
            this.variables.add(new HashMap<>());
        }
    }
}

class Visitor extends CtScanner {
    private final Scope scope;

    private final BiConsumer<? super Scope, ? super CtAssignment<?, ?>> visit;

    Visitor(BiConsumer<? super Scope, ? super CtAssignment<?, ?>> visit) {
        this.visit = visit;
        this.scope = new Scope();
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignment) {
        this.visit.accept(this.scope, assignment);
        super.visitCtAssignment(assignment);
    }

    @Override
    public <R> void visitCtBlock(CtBlock<R> block) {
        this.scope.push();
        super.visitCtBlock(block);
        this.scope.pop();
    }

    @Override
    public <R> void visitCtField(CtField<R> field) {
        this.scope.register(field.getReference(), field.getDefaultExpression());
        this.updateValueAssignment(field.getReference(), field.getAssignment());

        super.visitCtField(field);
    }

    @Override
    public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
        this.scope.register(localVariable.getReference(), localVariable.getDefaultExpression());
        this.updateValueAssignment(localVariable.getReference(), localVariable.getAssignment());

        super.visitCtLocalVariable(localVariable);
    }

    private <T> void updateValueAssignment(CtVariableReference<T> variable, CtExpression<?> value) {
        if (value instanceof CtNewArray<?> newArray) {
            CtLiteral<?> defaultValue = RedundantArrayInit.getDefaultArrayValueOrNull(variable.getType(), newArray);
            if (defaultValue != null) {
                this.scope.register(variable, defaultValue);
            }
        } else if (value instanceof CtLiteral<?> literal) {
            this.scope.register(variable, literal);
        } else {
            // an unknown kind of value has been assigned, therefore the variable is no longer tracked
            this.scope.remove(variable);
        }
    }

    @Override
    public <R> void visitCtVariableWrite(CtVariableWrite<R> variableWrite) {
        // if the variable has been assigned a new value, update it:
        if (variableWrite instanceof CtRHSReceiver<?> rhs) {
            this.updateValueAssignment(variableWrite.getVariable(), rhs.getAssignment());
        }
        super.visitCtVariableWrite(variableWrite);
    }
}

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_ARRAY_INIT })
public class RedundantArrayInit extends IntegratedCheck {
    private static final Map<String, CtLiteral<?>> DEFAULT_VALUES = Map.ofEntries(
        Map.entry("int", makeLiteral(0)),
        Map.entry("double", makeLiteral(0.0d)),
        Map.entry("float", makeLiteral(0.0f)),
        Map.entry("long", makeLiteral(0L)),
        Map.entry("short", makeLiteral((short) 0)),
        Map.entry("byte", makeLiteral((byte) 0)),
        Map.entry("char", makeLiteral((char) 0)),
        Map.entry("boolean", makeLiteral(false))
    );

    private static <T> CtLiteral<?> makeLiteral(T value) {
        CtLiteral<T> literal = new CtLiteralImpl<>();
        literal.setValue(value);
        return literal;
    }

    static <R> CtLiteral<?> getDefaultArrayValueOrNull(CtTypeInformation type, R rhs) {
        // skip all non-array assignments:
        if (!type.isArray()) {
            return null;
        }

        CtArrayTypeReference<?> arrayType = (CtArrayTypeReference<?>) type;

        // skip all non-default array assignments:
        if (!(rhs instanceof CtNewArray<?> newArray)) {
            return null;
        }

        if (!newArray.getElements().isEmpty()) {
            return null;
        }

        CtTypeReference<?> realType = arrayType.getArrayType();
        if (realType.isPrimitive() && !DEFAULT_VALUES.containsKey(realType.getSimpleName())) {
            // skip all types for which we don't have a default value
            return null;
        }

        if (realType.isPrimitive()) {
            return DEFAULT_VALUES.get(realType.getSimpleName());
        } else {
            return makeLiteral(null);
        }
    }

    public RedundantArrayInit() {
        super(new LocalizedMessage("redundant-array-init-desc"));
    }

    // equals impl of CtLiteral seems to be broken
    private static boolean areLiteralsEqual(
        CtLiteral<?> left,
        CtLiteral<?> right
    ) {
        if (left == null && right == null) {
            return true;
        } else if (left == null || right == null) {
            return false;
        }

        if (left.getValue() == null) {
            return right.getValue() == null;
        } else if (right.getValue() == null) {
            return false;
        }

        if (left.getValue() instanceof Character l && right.getValue() instanceof Character r) {
            return l.equals(r);
        } else if (left.getValue() instanceof Number l && right.getValue() instanceof Character r) {
            return l.intValue() == (int) r;
        } else if (left.getValue() instanceof Character l && right.getValue() instanceof Number r) {
            return (int) l == r.intValue();
        }

        if (!(left.getValue() instanceof Number valLeft)
            || !(right.getValue() instanceof Number valRight)) {
            return left.getValue() == right.getValue() || left.getValue().equals(right.getValue());
        }

        if (valLeft instanceof Float || valLeft instanceof Double || valRight instanceof Float
            || valRight instanceof Double) {
            return valLeft.doubleValue() == valRight.doubleValue();
        }

        return valLeft.longValue() == valRight.longValue();
    }

    private static CtVariableAccess<?> getVariableFromArray(CtArrayAccess<?, ?> ctArrayAccess) {
        CtExpression<?> array = ctArrayAccess.getTarget();

        if (array instanceof CtVariableAccess<?>) {
            return (CtVariableAccess<?>) array;
        } else if (array instanceof CtArrayAccess<?, ?>) {
            return getVariableFromArray((CtArrayAccess<?, ?>) array);
        } else {
            return null;
        }
    }

    private <L, R> void checkAssignment(
        L lhs,
        R rhs,
        CtElement element,
        Scope scope
    ) {
        // check if a new array is assigned to the variable and then put it into the scope
        if (lhs instanceof CtVariableWrite<?> write) {
            CtLiteral<?> defaultValue = getDefaultArrayValueOrNull(write.getType(), rhs);
            if (defaultValue != null) {
                scope.register(write.getVariable(), defaultValue);
            }

            return;
        }

        if (lhs instanceof CtArrayWrite<?> assigned) {
            CtVariableAccess<?> read = getVariableFromArray(assigned);
            // could not access the variable, should not happen...
            if (read == null) {
                return;
            }
            CtVariableReference<?> variableName = read.getVariable();

            if (scope.get(variableName) == null) {
                return;
            }

            CtLiteral<?> defaultValue = (CtLiteral<?>) scope.get(variableName);
            if (defaultValue == null) {
                // could not find the default value
                return;
            }

            // check if the assigned value is a literal. If it is, then check for
            // equality with the respective default for each type.
            if (!(rhs instanceof CtLiteral<?> rhsValue)
                || !areLiteralsEqual(rhsValue, defaultValue)) {
                // a non-default value has been assigned to the array
                // therefore it is removed from the pool of default values
                scope.remove(variableName);
                return;
            }

            // the assigned value is the default value, so it can be removed.
            this.addLocalProblem(
                element,
                new LocalizedMessage("redundant-array-init-desc"),
                ProblemType.REDUNDANT_ARRAY_INIT
            );
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new Visitor((scope, element) -> {
            this.checkAssignment(element.getAssigned(), element.getAssignment(), element, scope);
        }));
    }
}
