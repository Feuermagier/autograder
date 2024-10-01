package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.integrated.effects.AssignmentStatement;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import de.firemage.autograder.core.integrated.effects.TerminalStatement;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBodyHolder;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class StatementUtil {
    private StatementUtil() {
    }

    private static List<CtStatement> getEffectiveStatements(Collection<? extends CtStatement> statements) {
        return statements.stream().flatMap(ctStatement -> {
            // flatten blocks
            if (ctStatement instanceof CtStatementList ctStatementList) {
                return getEffectiveStatements(ctStatementList.getStatements()).stream();
            } else {
                return Stream.of(ctStatement);
            }
        }).filter(statement -> !(statement instanceof CtComment)).toList();
    }

    public static List<CtStatement> getEffectiveStatements(CtStatement ctStatement) {
        if (ctStatement == null) {
            return List.of();
        }

        if (ctStatement instanceof CtStatementList ctStatementList) {
            return getEffectiveStatements(ctStatementList.getStatements());
        }

        return getEffectiveStatements(List.of(ctStatement));
    }

    public static List<CtStatement> getEffectiveStatementsOf(CtBodyHolder ctBodyHolder) {
        if (ctBodyHolder == null) {
            return List.of();
        }

        CtStatement body = ctBodyHolder.getBody();
        if (body == null) {
            return List.of();
        }

        return getEffectiveStatements(body);
    }

    /**
     * Extracts a nested statement from a block if possible.
     * <p>
     * A statement might be in a block {@code { statement }}.
     * This method will extract the statement from the block and return it.
     *
     * @param statement the statement to unwrap
     * @return the given statement or an unwrapped version if possible
     */
    public static CtStatement unwrapStatement(CtStatement statement) {
        if (statement instanceof CtBlock<?> block) {
            List<CtStatement> statements = getEffectiveStatements(block);
            if (statements.size() == 1) {
                return statements.get(0);
            }
        }
        return statement;
    }

    private static <T> int referenceIndexOf(List<T> list, T element) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == element) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the statement that is before the given statement if possible.
     *
     * @param ctStatement the statement to find the previous statement of, must not be null
     * @return the previous statement or an empty optional if there is no previous statement
     */
    public static Optional<CtStatement> getPreviousStatement(CtStatement ctStatement) {
        List<CtStatement> previousStatements = getPreviousStatements(ctStatement);
        return previousStatements.isEmpty() ? Optional.empty() : Optional.of(previousStatements.get(previousStatements.size() - 1));
    }

    public static List<CtStatement> getPreviousStatements(CtStatement ctStatement) {
        List<CtStatement> result = new ArrayList<>();
        if (ctStatement.getParent() instanceof CtStatementList ctStatementList) {
            List<CtStatement> statements = ctStatementList.getStatements();
            int index = referenceIndexOf(statements, ctStatement);

            if (index >= 0) {
                result.addAll(statements.subList(0, index));
            }
        }

        return result;
    }

    public static List<CtStatement> getNextStatements(CtStatement ctStatement) {
        List<CtStatement> result = new ArrayList<>();
        if (ctStatement.getParent() instanceof CtStatementList ctStatementList) {
            List<CtStatement> statements = ctStatementList.getStatements();
            int index = referenceIndexOf(statements, ctStatement);

            if (index >= 0) {
                result.addAll(statements.subList(index + 1, statements.size()));
            }
        }

        return result;
    }

    public static Optional<Effect> tryMakeEffect(CtStatement ctStatement) {
        return TerminalStatement.of(ctStatement).or(() -> AssignmentStatement.of(ctStatement));
    }

    public static Optional<Effect> getSingleEffect(Collection<? extends CtStatement> ctStatements) {
        List<CtStatement> statements = getEffectiveStatements(ctStatements);

        if (statements.size() != 1 && (statements.size() != 2 || !(statements.get(1) instanceof CtBreak))) {
            return Optional.empty();
        }

        return tryMakeEffect(statements.get(0));
    }

    public static boolean isTerminal(CtStatement ctStatement) {
        List<CtStatement> statements = StatementUtil.getEffectiveStatements(ctStatement);

        return !statements.isEmpty() && tryMakeEffect(statements.get(statements.size() - 1)).map(TerminalEffect.class::isInstance).orElse(false);
    }

    public static List<Effect> getCasesEffects(Iterable<? extends CtCase<?>> ctCases) {
        List<Effect> effects = new ArrayList<>();
        for (CtCase<?> ctCase : ctCases) {
            Optional<Effect> effect = getSingleEffect(ctCase.getStatements());
            if (effect.isEmpty()) {
                return new ArrayList<>();
            }

            Effect resolvedEffect = effect.get();


            // check for default case, which is allowed to be a terminal effect, even if the other cases are not:
            if (ctCase.getCaseExpressions().isEmpty() && resolvedEffect instanceof TerminalEffect) {
                continue;
            }

            effects.add(resolvedEffect);
        }

        if (effects.isEmpty()) return new ArrayList<>();

        return effects;
    }
}
