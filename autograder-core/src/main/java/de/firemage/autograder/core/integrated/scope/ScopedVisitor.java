package de.firemage.autograder.core.integrated.scope;

import de.firemage.autograder.core.integrated.SpoonUtil;
import spoon.reflect.code.CtArrayWrite;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtRHSReceiver;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtField;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtScanner;

public class ScopedVisitor extends CtScanner {
    private final Scope scope;

    protected ScopedVisitor() {
        super();
        this.scope = new Scope();
    }

    protected Scope getScope() {
        return this.scope;
    }

    @Override
    public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignment) {
        if (assignment.getAssigned() instanceof CtVariableWrite<?> variable) {
            this.updateValueAssignment(variable.getVariable(), assignment.getAssignment());
        } else if (assignment.getAssigned() instanceof CtArrayWrite<?> arrayWrite) {
            this.scope.registerOrUpdateArray(arrayWrite, assignment.getAssignment());
        }

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
        this.scope.register(localVariable.getReference(), SpoonUtil.getDefaultValue(localVariable.getType()));
        this.updateValueAssignment(localVariable.getReference(), localVariable.getAssignment());

        super.visitCtLocalVariable(localVariable);
    }

    @Override
    public void visitCtFor(CtFor ctFor) {
        enter(ctFor);
        scan(CtRole.ANNOTATION, ctFor.getAnnotations());
        scan(CtRole.FOR_INIT, ctFor.getForInit());
        // remove all loop variables (which have no stable value):
        for (CtStatement statement : ctFor.getForInit()) {
            if (statement instanceof CtLocalVariable<?> variable) {
                this.scope.remove(variable.getReference());
            }
        }

        // TODO: deal with other loops and do reason about the loop variable
        // if for example all array indices are same value, the index does not matter

        scan(CtRole.EXPRESSION, ctFor.getExpression());
        scan(CtRole.FOR_UPDATE, ctFor.getForUpdate());
        scan(CtRole.BODY, ctFor.getBody());
        scan(CtRole.COMMENT, ctFor.getComments());
        exit(ctFor);
    }

    @Override
    public <R> void visitCtVariableWrite(CtVariableWrite<R> variableWrite) {
        // if the variable has been assigned a new value, update it:
        if (variableWrite instanceof CtRHSReceiver<?> rhs) {
            this.updateValueAssignment(variableWrite.getVariable(), rhs.getAssignment());
        }
        super.visitCtVariableWrite(variableWrite);
    }

    private <T> void updateValueAssignment(CtVariableReference<T> variable, CtExpression<?> value) {
        if (value != null) {
            this.scope.updateOrRegister(variable, value);
        }
    }
}
