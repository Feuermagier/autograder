package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.apache.commons.lang3.Range;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtTypedElement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

@ExecutableCheck(reportedProblems = { ProblemType.CHAR_RANGE })
public class CharRange extends IntegratedCheck {
    private static final Set<BinaryOperatorKind> RANGE_OPERATORS = Set.of(
        BinaryOperatorKind.LT,
        BinaryOperatorKind.LE,
        BinaryOperatorKind.GT,
        BinaryOperatorKind.GE
    );

    private static final Map<Range<Character>, UnaryOperator<String>> KNOWN_RANGES = Map.of(
        Range.between('a', 'z'), arg -> "Character.isAlphabetic(%s) && Character.isLowerCase(%s)".formatted(arg, arg),
        Range.between('A', 'Z'), arg -> "Character.isAlphabetic(%s) && Character.isUpperCase(%s)".formatted(arg, arg),
        Range.between('0', '9'), arg -> "Character.isDigit(%s)".formatted(arg)
    );

    private static boolean isOfTypeCharacter(CtTypedElement<?> ctTypedElement) {
        return SpoonUtil.isTypeEqualTo(ctTypedElement.getType(), java.lang.Character.class, char.class);
    }

    private static BinaryOperatorKind swapOperator(BinaryOperatorKind operator) {
        return switch (operator) {
            // a < b => b > a
            case LT -> BinaryOperatorKind.GT;
            // a <= b => b >= a
            case LE -> BinaryOperatorKind.GE;
            // a >= b => b <= a
            case GE -> BinaryOperatorKind.LE;
            // a > b => b < a
            case GT -> BinaryOperatorKind.LT;
            default -> operator;
        };
    }

    private record CtRange<T extends Comparable<T>>(
        CtExpression<T> ctExpression,
        BinaryOperatorKind operator,
        CtLiteral<T> ctLiteral) {

        @SuppressWarnings("unchecked")
        public static Optional<CtRange<Character>> of(CtBinaryOperator<Boolean> ctBinaryOperator) {
            // <expr> <op> <literal>
            CtExpression<Character> expression;
            BinaryOperatorKind operator;
            CtLiteral<Character> literal;

            // <expr> <op> <literal>
            if (SpoonUtil.resolveCtExpression(ctBinaryOperator.getRightHandOperand()) instanceof CtLiteral<?> ctLiteral
                && isOfTypeCharacter(ctLiteral)) {
                literal = (CtLiteral<Character>) ctLiteral;
                operator = ctBinaryOperator.getKind();
                expression = (CtExpression<Character>) ctBinaryOperator.getLeftHandOperand();
                // <literal> <op> <expr>
            } else if (SpoonUtil.resolveCtExpression(ctBinaryOperator.getLeftHandOperand()) instanceof CtLiteral<?> ctLiteral
                && isOfTypeCharacter(ctLiteral)) {
                literal = (CtLiteral<Character>) ctLiteral;
                expression = (CtExpression<Character>) ctBinaryOperator.getRightHandOperand();
                // must swap literal and expression, to do that, the operator must be inverted:
                // a <= b => b >= a
                // a < b => b > a
                // a >= b => b <= a
                // a > b => b < a
                operator = swapOperator(ctBinaryOperator.getKind());
            } else {
                return Optional.empty();
            }

            // adjust the literal if the operator is < or >
            if (operator == BinaryOperatorKind.LT) {
                // <expr> < <literal> => <expr> <= <literal> - 1
                literal.setValue((char) (literal.getValue() - 1));
                operator = BinaryOperatorKind.LE;
            } else if (ctBinaryOperator.getKind() == BinaryOperatorKind.GT) {
                // <expr> > <literal> => <expr> >= <literal> + 1
                literal.setValue((char) (literal.getValue() + 1));
                operator = BinaryOperatorKind.GE;
            }

            return Optional.of(new CtRange<>(expression, operator, literal));
        }

        private static <T> CtLiteral<T> minValue(CtLiteral<T> ctLiteral) {
            CtLiteral result = ctLiteral.getFactory().createLiteral();
            result.setBase(ctLiteral.getBase());

            if (ctLiteral.getValue() instanceof Integer) {
                result.setValue(Integer.MIN_VALUE);
            } else if (ctLiteral.getValue() instanceof Character) {
                result.setValue(Character.MIN_VALUE);
            } else if (ctLiteral.getValue() instanceof Long) {
                result.setValue(Long.MIN_VALUE);
            }

            return result;
        }

        private static <T> CtLiteral<T> maxValue(CtLiteral<T> ctLiteral) {
            CtLiteral result = ctLiteral.getFactory().createLiteral();
            result.setBase(ctLiteral.getBase());

            if (ctLiteral.getValue() instanceof Integer) {
                result.setValue(Integer.MAX_VALUE);
            } else if (ctLiteral.getValue() instanceof Character) {
                result.setValue(Character.MAX_VALUE);
            } else if (ctLiteral.getValue() instanceof Long) {
                result.setValue(Long.MAX_VALUE);
            }

            return result;
        }

        public Range<T> toRange() {
            // <expr> <op> <literal>
            if (this.operator == BinaryOperatorKind.LE) {
                return Range.between(minValue(this.ctLiteral).getValue(), this.ctLiteral.getValue());
            } else if (this.operator == BinaryOperatorKind.GE) {
                // <expr> >= <literal>
                return Range.between(this.ctLiteral.getValue(), maxValue(this.ctLiteral).getValue());
            } else {
                throw new IllegalStateException("Unsupported operator: " + this.operator);
            }
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtBinaryOperator<Boolean>>() {
            @Override
            @SuppressWarnings("unchecked")
            public void process(CtBinaryOperator<Boolean> ctBinaryOperator) {
                if (ctBinaryOperator.isImplicit()
                    || !ctBinaryOperator.getPosition().isValidPosition()
                    || !SpoonUtil.isTypeEqualTo(ctBinaryOperator.getType(), java.lang.Boolean.class, boolean.class)
                    || ctBinaryOperator.getKind() != BinaryOperatorKind.AND
                    || !(ctBinaryOperator.getLeftHandOperand() instanceof CtBinaryOperator<?> left)
                    || !(ctBinaryOperator.getRightHandOperand() instanceof CtBinaryOperator<?> right)
                    || !RANGE_OPERATORS.contains(left.getKind())
                    || !RANGE_OPERATORS.contains(right.getKind())
                ) {
                    return;
                }

                // structure must be one of:
                // - (<expr> <op> <literal>) && (<expr> <op> <literal>)
                // - (<literal> <op> <expr>) && (<literal> <op> <expr>)
                // or swapped

                CtRange<Character> leftRange = CtRange.of((CtBinaryOperator<Boolean>) left).orElse(null);
                CtRange<Character> rightRange = CtRange.of((CtBinaryOperator<Boolean>) right).orElse(null);
                if (leftRange == null || rightRange == null) {
                    return;
                }

                // skip them if they do not check the same variable
                if (!leftRange.ctExpression().equals(rightRange.ctExpression())) {
                    return;
                }

                Range<Character> intersection = leftRange.toRange().intersectionWith(rightRange.toRange());

                UnaryOperator<String> suggester = KNOWN_RANGES.get(intersection);
                if (suggester != null) {
                    addLocalProblem(
                        ctBinaryOperator,
                        new LocalizedMessage(
                            "char-range",
                            Map.of("suggestion", suggester.apply(leftRange.ctExpression().prettyprint()))
                        ),
                        ProblemType.CHAR_RANGE
                    );
                }
            }
        });
    }
}
