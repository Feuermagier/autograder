package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAbstractSwitch;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.CtYieldStatement;
import spoon.reflect.reference.CtVariableReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

interface Effect {
    CtStatement ctStatement();

    Optional<CtExpression<?>> value();

    boolean isSameEffect(Effect other);
}

interface TerminalEffect extends Effect {
}

interface AssignmentEffect extends Effect {
    CtVariableReference<?> target();
}

class AssignmentStatement implements AssignmentEffect {
    private final CtAssignment<?, ?> ctAssignment;
    private final Optional<CtExpression<?>> value;

    private AssignmentStatement(CtAssignment<?, ?> ctAssignment) {
        this.ctAssignment = ctAssignment;
        this.value = Optional.ofNullable(ctAssignment.getAssignment());
    }

    public static Optional<Effect> of(CtStatement ctStatement) {
        if (ctStatement instanceof CtAssignment<?, ?> ctAssignment && ctAssignment.getAssigned() instanceof CtVariableWrite<?>) {
            return Optional.of(new AssignmentStatement(ctAssignment));
        }

        return Optional.empty();
    }

    @Override
    public CtStatement ctStatement() {
        return this.ctAssignment;
    }

    @Override
    public Optional<CtExpression<?>> value() {
        return this.value;
    }

    @Override
    public CtVariableReference<?> target() {
        return ((CtVariableWrite<?>) this.ctAssignment.getAssigned()).getVariable();
    }

    @Override
    public boolean isSameEffect(Effect other) {
        return other instanceof AssignmentEffect otherAssignment && this.target().equals(otherAssignment.target());
    }
}

class TerminalStatement implements TerminalEffect {
    private final CtStatement ctStatement;
    private final Optional<CtExpression<?>> ctExpression;

    private TerminalStatement(CtStatement ctStatement) {
        if (!isTerminalStatement(ctStatement)) {
            throw new IllegalArgumentException("Not a terminal statement: " + ctStatement);
        }

        this.ctStatement = ctStatement;

        if (ctStatement instanceof CtReturn<?> ctReturn) {
            this.ctExpression = Optional.ofNullable(ctReturn.getReturnedExpression());
        } else if (ctStatement instanceof CtThrow ctThrow) {
            this.ctExpression = Optional.of(ctThrow.getThrownExpression());
        } else if (ctStatement instanceof CtYieldStatement ctYieldStatement) {
            this.ctExpression = Optional.of(ctYieldStatement.getExpression());
        } else {
            this.ctExpression = Optional.empty();
        }
    }

    private static boolean isTerminalStatement(CtStatement ctStatement) {
        return ctStatement instanceof CtYieldStatement
                || ctStatement instanceof CtReturn<?>
                || ctStatement instanceof CtThrow;
    }

    public static Optional<Effect> of(CtStatement ctStatement) {
        if (isTerminalStatement(ctStatement)) {
            return Optional.of(new TerminalStatement(ctStatement));
        }

        return Optional.empty();
    }

    @Override
    public CtStatement ctStatement() {
        return this.ctStatement;
    }

    @Override
    public Optional<CtExpression<?>> value() {
        return this.ctExpression;
    }

    @Override
    public boolean isSameEffect(Effect other) {
        if (other instanceof TerminalEffect) {
            return this.ctStatement instanceof CtYieldStatement && other.ctStatement() instanceof CtYieldStatement
                    || this.ctStatement instanceof CtReturn<?> && other.ctStatement() instanceof CtReturn<?>
                    || this.ctStatement instanceof CtThrow && other.ctStatement() instanceof CtThrow;
        }

        return false;
    }
}

@ExecutableCheck(reportedProblems = {ProblemType.SHOULD_BE_ENUM_ATTRIBUTE})
public class ShouldBeEnumAttribute extends IntegratedCheck {
    public ShouldBeEnumAttribute() {
        super(new LocalizedMessage("should-be-enum-attribute"));
    }

    public static Optional<Effect> tryMakeEffect(CtStatement ctStatement) {
        return TerminalStatement.of(ctStatement).or(() -> AssignmentStatement.of(ctStatement));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtAbstractSwitch<?>>() {
            @Override
            public void process(CtAbstractSwitch<?> ctSwitch) {
                // skip switch statements that are not on an enum
                if (!ctSwitch.getSelector().getType().isEnum()) {
                    return;
                }

                List<Effect> effects = new ArrayList<>();
                for (CtCase<?> ctCase : ctSwitch.getCases()) {
                    List<CtStatement> statements = SpoonUtil.getEffectiveStatements(ctCase);

                    if (statements.size() != 1 && (statements.size() != 2 || !(statements.get(1) instanceof CtBreak))) {
                        return;
                    }

                    Optional<Effect> effect = tryMakeEffect(statements.get(0));
                    if (effect.isEmpty()) {
                        return;
                    }

                    Effect resolvedEffect = effect.get();


                    // check for default case, which is allowed to be a terminal effect, even if the other cases are not:
                    if (ctCase.getCaseExpressions().isEmpty() && resolvedEffect instanceof TerminalEffect) {
                        continue;
                    }

                    effects.add(resolvedEffect);
                }

                if (effects.isEmpty()) return;

                Effect firstEffect = effects.get(0);
                for (Effect effect : effects) {
                    if (!firstEffect.isSameEffect(effect)) {
                        return;
                    }

                    Optional<CtExpression<?>> ctExpression = effect.value();
                    if (ctExpression.isEmpty()) {
                        return;
                    }

                    if (!(ctExpression.get() instanceof CtLiteral<?>)) {
                        return;
                    }
                }

                addLocalProblem(
                    ctSwitch,
                    new LocalizedMessage("should-be-enum-attribute"),
                    ProblemType.SHOULD_BE_ENUM_ATTRIBUTE
                );
            }
        });
    }
}
