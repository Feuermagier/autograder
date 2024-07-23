package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StatementUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import de.firemage.autograder.core.integrated.UsesFinder;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;

import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = {ProblemType.FIELD_SHOULD_BE_FINAL})
public class FieldShouldBeFinal extends IntegratedCheck {
    /**
     * Checks if a field can be final.
     * <p>
     * A returned value of false does not guarantee that the field can not be final.
     *
     * @param ctField the field to check
     * @return true if the field can be final, false otherwise
     * @param <T> the type of the field
     */
    private static <T> boolean canBeFinal(CtField<T> ctField) {
        if (ctField.isFinal()) {
            return true;
        }

        // if the field has any write that is not in a constructor, it can not be final
        if (!(ctField.getDeclaringType() instanceof CtClass<?> ctClass)
            || UsesFinder.variableWrites(ctField).hasAnyMatch(ctFieldWrite -> ctFieldWrite.getParent(CtConstructor.class) == null)) {
            return false;
        }

        if (ctField.isProtected() && TypeUtil.hasSubtype(ctClass)) {
            return false;
        }

        // we need to check if the field is explicitly initialized, because this is not allowed:
        //
        // final String a = "hello";
        // a = "world"; // error
        //
        // but this is allowed:
        //
        // final String a;
        // a = "hello";
        boolean hasExplicitValue = ctField.getDefaultExpression() != null && !ctField.getDefaultExpression().isImplicit();

        // Static fields and final is complicated.
        //
        // The check must not be able to recognize any edge case. It is enough when it is able
        // to recognize the most important cases.
        //
        // For static fields, this would be an explicit value and no write in the whole program.
        if (ctField.isStatic()) {
            return hasExplicitValue && !UsesFinder.variableWrites(ctField).hasAny();
        }

        // for a field to be final, it must be written to exactly once in each code path of each constructor.
        //
        // if the field is not written to, an implicit value is used, which is fine
        // if the field is written to more than once, it can not be final
        int allowedWrites = 1;
        if (hasExplicitValue) {
            // if the field has an explicit value, it must not be written to more than once to be final
            allowedWrites = 0;
        }

        // NOTE: I did not implement the code path checking, e.g.
        //
        // if (condition) {
        //   this.field = 1;
        // } else {
        //   this.field = 2;
        // }
        //
        // that is complicated to implement correctly.

        for (CtConstructor<?> ctConstructor : ctClass.getConstructors()) {
            // an implicit constructor is the default one
            // -> that constructor does not write to any of the fields
            if (ctConstructor.isImplicit() && allowedWrites != 0) {
                return false;
            }

            int mainPathWrites = 0;
            int otherPathWrites = 0;
            for (CtStatement ctStatement : StatementUtil.getEffectiveStatementsOf(ctConstructor)) {
                if (ctStatement instanceof CtAssignment<?,?> ctAssignment && UsesFinder.variableWrites(ctField).nestedIn(ctAssignment).hasAny()) {
                    mainPathWrites += 1;
                } else if (UsesFinder.variableWrites(ctField).nestedIn(ctStatement).hasAny()) {
                    otherPathWrites += 1;
                }
            }

            // we have the main path, e.g. the constructor body where each statement is guaranteed to be executed
            // and the other path, statements that are only executed under certain conditions

            if (mainPathWrites != allowedWrites || otherPathWrites != 0) {
                return false;
            }
        }

        // we reached here -> the field has exactly one write in each constructor
        return true;
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> ctField) {
                if (ctField.isImplicit() || !ctField.getPosition().isValidPosition() || ctField.isFinal()) {
                    return;
                }

                if (canBeFinal(ctField)) {
                    addLocalProblem(
                        ctField,
                        new LocalizedMessage(
                            "field-should-be-final",
                            Map.of("name", ctField.getSimpleName())
                        ),
                        ProblemType.FIELD_SHOULD_BE_FINAL
                    );
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(4);
    }
}
