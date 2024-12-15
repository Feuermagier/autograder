package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.MethodUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.visitor.CtScanner;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Checks for magic literals in the code.
 *
 * @author Tobias Thirolf
 * @author Lucas Altenau
 */
@ExecutableCheck(reportedProblems = { ProblemType.MAGIC_LITERAL })
public class MagicLiteral extends IntegratedCheck {
    private static final Set<Double> DEFAULT_IGNORED_NUMBERS = Set.of(-1.0, 0.0, 1.0, 2.0);

    private <T> void visitLiteral(String magicType, CtLiteral<T> ctLiteral) {
        CtMethod<?> parentMethod = ctLiteral.getParent(CtMethod.class);
        // allow magic literals in hashCode methods (some implementations use prime numbers)
        if (parentMethod != null && TypeUtil.isTypeEqualTo(parentMethod.getType(), int.class) && parentMethod.getSimpleName().equals("hashCode")
            && MethodUtil.isOverriddenMethod(parentMethod)) {
            return;
        }

        CtField<?> parent = ctLiteral.getParent(CtField.class);
        if (parent == null || !parent.isFinal()) {
            this.addLocalProblem(
                ctLiteral,
                new LocalizedMessage(
                    "magic-literal",
                    Map.of(
                        "value", formatValue(ctLiteral.getValue()),
                        "type", magicType
                    )
                ),
                ProblemType.MAGIC_LITERAL
            );
        }
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String string) {
            return "\"%s\"".formatted(string.replace("\n", "\\n").replace("\r", "\\r"));
        } else if (value instanceof Character) {
            return "'%s'".formatted(value);
        } else {
            return value.toString();
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        CtModel submissionModel = staticAnalysis.getModel();

        for (CtModule module : submissionModel.getAllModules()) {
            module.accept(new CtScanner() {
                @Override
                public <T> void visitCtLiteral(CtLiteral<T> ctLiteral) {
                    if (ctLiteral.isImplicit() || !ctLiteral.getPosition().isValidPosition() || ctLiteral.getType() == null) {
                        super.visitCtLiteral(ctLiteral);
                        return;
                    }

                    if (ctLiteral.getType().isPrimitive()) {
                        if (ctLiteral.getValue() instanceof Number number && !DEFAULT_IGNORED_NUMBERS.contains(number.doubleValue())) {
                            visitLiteral("number", ctLiteral);
                        } else if (ctLiteral.getValue() instanceof Character) {
                            visitLiteral("char", ctLiteral);
                        }
                    } else if (ctLiteral.getValue() instanceof String string && !string.isEmpty()) {
                        visitLiteral("string", ctLiteral);
                    }

                    super.visitCtLiteral(ctLiteral);
                }
            });
        }
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(1);
    }
}
