package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.integrated.effects.AssignmentStatement;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import de.firemage.autograder.core.integrated.effects.TerminalStatement;
import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import de.firemage.autograder.core.integrated.evaluator.fold.FoldUtils;
import de.firemage.autograder.core.integrated.evaluator.fold.InferOperatorTypes;
import de.firemage.autograder.core.integrated.evaluator.fold.InlineVariableRead;
import de.firemage.autograder.core.integrated.evaluator.fold.RemoveRedundantCasts;
import org.apache.commons.io.FilenameUtils;
import spoon.reflect.CtModel;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtTypePattern;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.LiteralBase;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.CompoundSourcePosition;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.eval.PartialEvaluator;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class SpoonUtil {
    private SpoonUtil() {

    }

    public static boolean isInJunitTest() {
        return Arrays.stream(Thread.currentThread().getStackTrace())
            .anyMatch(element -> element.getClassName().startsWith("org.junit."));
    }

    public static boolean isString(CtTypeReference<?> type) {
        return isTypeEqualTo(type, java.lang.String.class);
    }

    public static Optional<CtTypeReference<?>> isToStringCall(CtExpression<?> expression) {
        if (!SpoonUtil.isString(expression.getType())) {
            return Optional.empty();
        }

        if (expression instanceof CtInvocation<?> invocation &&
            SpoonUtil.isSignatureEqualTo(invocation.getExecutable(), java.lang.String.class, "toString")) {
            return Optional.of(invocation.getTarget().getType());
        } else {
            return Optional.empty();
        }
    }

    public static boolean isStringLiteral(CtExpression<?> expression, String value) {
        return expression instanceof CtLiteral<?> literal && literal.getValue() != null &&
            literal.getValue().equals(value);
    }

    public static boolean isNullLiteral(CtExpression<?> expression) {
        return SpoonUtil.resolveConstant(expression) instanceof CtLiteral<?> literal && literal.getValue() == null;
    }

    public static boolean isIntegerLiteral(CtExpression<?> expression, int value) {
        return expression instanceof CtLiteral<?> literal && literal.getValue().equals(value);
    }

    public static boolean isBoolean(CtTypedElement<?> ctTypedElement) {
        CtTypeReference<?> ctTypeReference = ctTypedElement.getType();
        return ctTypeReference != null && SpoonUtil.isTypeEqualTo(ctTypeReference, boolean.class, Boolean.class);
    }

    public static Optional<Boolean> tryGetBooleanLiteral(CtExpression<?> expression) {
        if (SpoonUtil.resolveConstant(expression) instanceof CtLiteral<?> literal
            && literal.getValue() != null
            && isBoolean(literal)) {

            return Optional.of((Boolean) literal.getValue());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> tryGetStringLiteral(CtExpression<?> expression) {
        if (SpoonUtil.resolveConstant(expression) instanceof CtLiteral<?> literal
            && literal.getValue() != null
            && isTypeEqualTo(literal.getType(), java.lang.String.class)) {

            return Optional.of((String) literal.getValue());
        } else {
            return Optional.empty();
        }
    }

    // equals impl of CtLiteral seems to be broken
    public static boolean areLiteralsEqual(
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

    @SuppressWarnings("unchecked")
    public static <T> CtLiteral<T> makeLiteralNumber(CtTypeReference<T> ctTypeReference, Number number) {
        Object value = FoldUtils.convert(ctTypeReference, number);

        return SpoonUtil.makeLiteral(ctTypeReference, (T) value);
    }

    /**
     * Makes a new literal with the given value and type.
     *
     * @param ctTypeReference a reference to the type of the literal
     * @param value the value of the literal
     * @param <T>   the type of the value
     *
     * @return a new literal with the given value, note that the base is not set
     */
    public static <T> CtLiteral<T> makeLiteral(CtTypeReference<T> ctTypeReference, T value) {
        CtLiteral<T> literal = ctTypeReference.getFactory().createLiteral();
        literal.setType(ctTypeReference.clone());
        literal.setValue(value);
        return literal;
    }

    public static List<CtExpression<?>> getElementsOfExpression(CtExpression<?> ctExpression) {
        var supportedCollections = Stream.of(
            java.util.List.class,
            java.util.Set.class,
            java.util.Collection.class
        ).map((Class<?> e) -> ctExpression.getFactory().Type().createReference(e));

        List<CtExpression<?>> result = new ArrayList<>();

        CtTypeReference<?> expressionType = ctExpression.getType();
        if (supportedCollections.noneMatch(ty -> ty.equals(expressionType) || expressionType.isSubtypeOf(ty))) {
            return result;
        }

        if (ctExpression instanceof CtInvocation<?> ctInvocation
            && ctInvocation.getTarget() instanceof CtTypeAccess<?>) {
            CtExecutableReference<?> ctExecutableReference = ctInvocation.getExecutable();
            if (ctExecutableReference.getSimpleName().equals("of")) {
                result.addAll(ctInvocation.getArguments());
            }
        }

        return result;
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


    public static <T> CtLiteral<T> minimumValue(CtLiteral<T> ctLiteral) {
        CtLiteral result = ctLiteral.getFactory().createLiteral();
        result.setBase(LiteralBase.DECIMAL);
        result.setType(ctLiteral.getType().clone());

        // - byte
        // - short
        // - int
        // - long
        // - float
        // - double
        // - boolean
        // - char

        Object value = ctLiteral.getValue();
        Map<Class<?>, Object> minimumValueMapping = Map.ofEntries(
            Map.entry(byte.class, Byte.MIN_VALUE),
            Map.entry(Byte.class, Byte.MIN_VALUE),
            Map.entry(short.class, Short.MIN_VALUE),
            Map.entry(Short.class, Short.MIN_VALUE),
            Map.entry(int.class, Integer.MIN_VALUE),
            Map.entry(Integer.class, Integer.MIN_VALUE),
            Map.entry(long.class, Long.MIN_VALUE),
            Map.entry(Long.class, Long.MIN_VALUE),
            Map.entry(float.class, Float.MIN_VALUE),
            Map.entry(Float.class, Float.MIN_VALUE),
            Map.entry(double.class, Double.MIN_VALUE),
            Map.entry(Double.class, Double.MIN_VALUE),
            Map.entry(boolean.class, false),
            Map.entry(Boolean.class, false),
            Map.entry(char.class, Character.MIN_VALUE),
            Map.entry(Character.class, Character.MIN_VALUE)
        );

        result.setValue(minimumValueMapping.get(value.getClass()));

        return result;
    }

    public static <T> CtLiteral<T> maximumValue(CtLiteral<T> ctLiteral) {
        CtLiteral result = ctLiteral.getFactory().createLiteral();
        result.setBase(LiteralBase.DECIMAL);
        result.setType(ctLiteral.getType().clone());

        // - byte
        // - short
        // - int
        // - long
        // - float
        // - double
        // - boolean
        // - char

        Object value = ctLiteral.getValue();
        Map<Class<?>, Object> maximumValueMapping = Map.ofEntries(
            Map.entry(byte.class, Byte.MAX_VALUE),
            Map.entry(Byte.class, Byte.MAX_VALUE),
            Map.entry(short.class, Short.MAX_VALUE),
            Map.entry(Short.class, Short.MAX_VALUE),
            Map.entry(int.class, Integer.MAX_VALUE),
            Map.entry(Integer.class, Integer.MAX_VALUE),
            Map.entry(long.class, Long.MAX_VALUE),
            Map.entry(Long.class, Long.MAX_VALUE),
            Map.entry(float.class, Float.MAX_VALUE),
            Map.entry(Float.class, Float.MAX_VALUE),
            Map.entry(double.class, Double.MAX_VALUE),
            Map.entry(Double.class, Double.MAX_VALUE),
            Map.entry(boolean.class, true),
            Map.entry(Boolean.class, true),
            Map.entry(char.class, Character.MAX_VALUE),
            Map.entry(Character.class, Character.MAX_VALUE)
        );

        result.setValue(maximumValueMapping.get(value.getClass()));

        return result;
    }

    /**
     * Converts a binary operator like 'a < b' to 'a <= b - 1' or 'a > b' to 'a >= b + 1'.
     *
     * @param ctBinaryOperator the operator to normalize, can be of any kind
     * @return the normalized operator or the given operator if it is not supported
     * @param <T> the type the operator evaluates to
     */
    private static <T> CtBinaryOperator<T> normalize(CtBinaryOperator<T> ctBinaryOperator) {
        // the following primitive types exist:
        // - byte
        // - short
        // - int
        // - long
        // - float
        // - double
        // - boolean
        // - char
        //
        // of those the following are not `Number`:
        // - boolean
        // - char

        if (!Set.of(BinaryOperatorKind.LT, BinaryOperatorKind.GT).contains(ctBinaryOperator.getKind())
            || !ctBinaryOperator.getRightHandOperand().getType().isPrimitive()) {
            return ctBinaryOperator;
        }

        // the literal to add/subtract. Simply setting it to 1 is not enough, because
        // 1 is of type int and the other side might for example be a double or float
        CtLiteral step = ctBinaryOperator.getFactory().Core().createLiteral();

        Predicate<CtTypeReference<?>> isCharacter = ty -> SpoonUtil.isTypeEqualTo(ty, char.class, java.lang.Character.class);
        if (isCharacter.test(ctBinaryOperator.getRightHandOperand().getType())) {
            // for character use an integer literal
            step.setValue((char) 1);
            step.setType(ctBinaryOperator.getFactory().Type().characterPrimitiveType());
        } else {
            // this assumes that < and > are only used with numbers
            step.setValue(FoldUtils.convert(ctBinaryOperator.getRightHandOperand().getType(), ((Number) 1).doubleValue()));
            step.setType(ctBinaryOperator.getRightHandOperand().getType());
        }

        CtBinaryOperator<T> result = ctBinaryOperator.clone();
        if (ctBinaryOperator.getKind() == BinaryOperatorKind.LT) {
            // <lhs> < <rhs> => <lhs> <= <rhs> - 1
            result.setKind(BinaryOperatorKind.LE);
            result.setRightHandOperand(SpoonUtil.createBinaryOperator(
                ctBinaryOperator.getRightHandOperand(),
                step,
                BinaryOperatorKind.MINUS
            ));
        } else if (ctBinaryOperator.getKind() == BinaryOperatorKind.GT) {
            // <lhs> > <rhs> => <lhs> >= <rhs> + 1
            result.setKind(BinaryOperatorKind.GE);
            result.setRightHandOperand(SpoonUtil.createBinaryOperator(
                ctBinaryOperator.getRightHandOperand(),
                step,
                BinaryOperatorKind.PLUS
            ));
        }

        // simplify the resulting operator
        result.setLeftHandOperand(SpoonUtil.resolveCtExpression(result.getLeftHandOperand()));
        // if the operand was a literal, it might have been promoted
        if (result.getLeftHandOperand() instanceof CtLiteral<?> ctLiteral) {
            result.setLeftHandOperand(SpoonUtil.castLiteral(
                SpoonUtil.getExpressionType(ctBinaryOperator.getLeftHandOperand()),
                ctLiteral
            ));
        }

        result.setRightHandOperand(SpoonUtil.resolveCtExpression(result.getRightHandOperand()));
        if (result.getRightHandOperand() instanceof CtLiteral<?> ctLiteral) {
            result.setRightHandOperand(SpoonUtil.castLiteral(
                SpoonUtil.getExpressionType(ctBinaryOperator.getRightHandOperand()),
                ctLiteral
            ));
        }

        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> CtBinaryOperator<T> createBinaryOperator(
        CtExpression<?> leftHandOperand,
        CtExpression<?> rightHandOperand,
        BinaryOperatorKind operatorKind
    ) {
        Factory factory = leftHandOperand.getFactory();
        if (factory == null) {
            factory = rightHandOperand.getFactory();
        }

        CtBinaryOperator ctBinaryOperator = factory.createBinaryOperator(
            leftHandOperand.clone(),
            rightHandOperand.clone(),
            operatorKind
        );

        if (ctBinaryOperator.getType() == null) {
            ctBinaryOperator.setType(FoldUtils.inferType(ctBinaryOperator));
        }

        return ctBinaryOperator;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static <T> CtUnaryOperator<T> createUnaryOperator(UnaryOperatorKind operatorKind, CtExpression<?> ctExpression) {
        CtUnaryOperator ctUnaryOperator = ctExpression.getFactory().createUnaryOperator();
        ctUnaryOperator.setOperand(ctExpression.clone());
        ctUnaryOperator.setKind(operatorKind);

        if (ctUnaryOperator.getType() == null) {
            ctUnaryOperator.setType(FoldUtils.inferType(ctUnaryOperator));
        }

        return ctUnaryOperator;
    }

    /**
     * Swaps the operands of a binary operator.
     *
     * @param ctBinaryOperator the operator to swap, can be of any kind
     * @return the cloned version with the operands swapped or the given operator if it is not supported
     * @param <T> the type the operator evaluates to
     */
    @SuppressWarnings({"unchecked","rawtypes"})
    public static <T> CtBinaryOperator<T> swapCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        CtBinaryOperator result = ctBinaryOperator.clone();

        CtExpression<?> left = result.getLeftHandOperand();
        CtExpression<?> right = result.getRightHandOperand();

        // NOTE: this only implements a few cases, for other non-commutative operators, this will break code
        result.setKind(switch (ctBinaryOperator.getKind()) {
            // a < b => b > a
            case LT -> BinaryOperatorKind.GT;
            // a <= b => b >= a
            case LE -> BinaryOperatorKind.GE;
            // a >= b => b <= a
            case GE -> BinaryOperatorKind.LE;
            // a > b => b < a
            case GT -> BinaryOperatorKind.LT;
            default -> ctBinaryOperator.getKind();
        });

        // swap the left and right
        result.setLeftHandOperand(right);
        result.setRightHandOperand(left);

        return result;
    }

    /**
     * Replaces {@link spoon.reflect.code.CtVariableRead} in the provided expression if they are effectively final
     * and their value is known.
     * <p>
     * Additionally, it will fix broken operators that do not have a type.
     *
     * @param ctExpression the expression to resolve. If it is {@code null}, then {@code null} is returned
     * @return the resolved expression. It will be cloned and detached from the {@link CtModel}
     * @param <T> the type of the expression
     */
    public static <T> CtExpression<T> resolveConstant(CtExpression<T> ctExpression) {
        if (ctExpression == null) return null;

        PartialEvaluator evaluator = new Evaluator(
            InferOperatorTypes.create(),
            InlineVariableRead.create()
        );

        return evaluator.evaluate(ctExpression);
    }

    /**
     * Converts a binary operator like &lt; to &lt;= or > to >= and adjusts the operands accordingly
     * to make finding patterns on them easier by not having to special-case them. Additionally,
     * one can specify a predicate to swap the operands if necessary. For example, to ensure that
     * a literal is always on the right-hand side.
     *
     * @param shouldSwap the left and right hands are passed to it, and it should return true if they should be swapped and false if nothing should be changed
     * @param ctBinaryOperator the operator to normalize, can be of any kind
     * @return the normalized operator or the given operator if it is not supported
     * @param <T> the type the operator evaluates to
     */
    public static <T> CtBinaryOperator<T> normalizeBy(
        BiPredicate<? super CtExpression<?>, ? super CtExpression<?>> shouldSwap,
        CtBinaryOperator<T> ctBinaryOperator
    ) {
        CtExpression<?> left = SpoonUtil.resolveConstant(ctBinaryOperator.getLeftHandOperand());
        CtExpression<?> right = SpoonUtil.resolveConstant(ctBinaryOperator.getRightHandOperand());
        BinaryOperatorKind operator = ctBinaryOperator.getKind();

        CtBinaryOperator<T> result = ctBinaryOperator.clone();
        result.setKind(operator);
        result.setLeftHandOperand(left.clone());
        result.setRightHandOperand(right.clone());

        // check if the left and right have to be swapped. To do that, the operator must be inverted:
        // a <= b => b >= a
        // a < b => b > a
        // a >= b => b <= a
        // a > b => b < a
        //
        // ^ in this example it is expected that the b should be on the left
        if (shouldSwap.test(left, right)) {
            result = swapCtBinaryOperator(result);
        }

        // in this step < and > are adjusted to <= and >= :
        // a < b => a <= b - 1
        // a > b => a >= b + 1

        return normalize(result);
    }

    @SuppressWarnings("unchecked")
    public static <T> CtExpression<T> negate(CtExpression<T> ctExpression) {
        // !(!(a)) => a
        if (ctExpression instanceof CtUnaryOperator<T> ctUnaryOperator && ctUnaryOperator.getKind() == UnaryOperatorKind.NOT) {
            return (CtExpression<T>) ctUnaryOperator.getOperand();
        }

        if (ctExpression instanceof CtBinaryOperator<T> ctBinaryOperator) {
            CtBinaryOperator<T> result = ctBinaryOperator.clone();
            switch (ctBinaryOperator.getKind()) {
                // !(a == b) -> a != b
                case EQ -> {
                    result.setKind(BinaryOperatorKind.NE);
                    return result;
                }
                // !(a != b) -> a == b
                //
                // a | b | a ^ b
                // 0 | 0 |   0
                // 0 | 1 |   1
                // 1 | 0 |   1
                // 1 | 1 |   0
                // => !(a ^ b) -> a == b
                case NE, BITXOR -> {
                    result.setKind(BinaryOperatorKind.EQ);
                    return result;
                }
                // !(a && b) -> !a || !b
                case AND -> {
                    result.setKind(BinaryOperatorKind.OR);
                    result.setLeftHandOperand(negate(result.getLeftHandOperand()));
                    result.setRightHandOperand(negate(result.getRightHandOperand()));
                    return result;
                }
                // !(a || b) -> !a && !b
                case OR -> {
                    result.setKind(BinaryOperatorKind.AND);
                    result.setLeftHandOperand(negate(result.getLeftHandOperand()));
                    result.setRightHandOperand(negate(result.getRightHandOperand()));
                    return result;
                }
                // !(a >= b) -> a < b
                case GE -> {
                    result.setKind(BinaryOperatorKind.LT);
                    return result;
                }
                // !(a > b) -> a <= b
                case GT -> {
                    result.setKind(BinaryOperatorKind.LE);
                    return result;
                }
                // !(a <= b) -> a > b
                case LE -> {
                    result.setKind(BinaryOperatorKind.GT);
                    return result;
                }
                // !(a < b) -> a >= b
                case LT -> {
                    result.setKind(BinaryOperatorKind.GE);
                    return result;
                }
            }
        }

        return createUnaryOperator(UnaryOperatorKind.NOT, ctExpression.clone());
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

    public static <T> CtExpression<T> resolveCtExpression(CtExpression<T> ctExpression) {
        if (ctExpression == null) return null;

        // Spoon's partiallyEvaluate is broken, not configurable, and fixing it would be too much work.
        // Therefore, we use our own implementation.
        PartialEvaluator evaluator = new Evaluator();

        return evaluator.evaluate(ctExpression);
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
            List<CtStatement> statements = SpoonUtil.getEffectiveStatements(block);
            if (statements.size() == 1) {
                return statements.get(0);
            }
        }
        return statement;
    }

    public static boolean isGetter(CtMethod<?> method) {
        return method.getSimpleName().startsWith("get")
               && method.getParameters().isEmpty()
               && !method.getType().getSimpleName().equals("void")
               && (method.isAbstract() || getEffectiveStatements(method.getBody()).size() == 1);
    }

    public static boolean isSetter(CtMethod<?> method) {
        return method.getSimpleName().startsWith("set")
               && method.getParameters().size() == 1
               && method.getType().getSimpleName().equals("void")
               && (method.isAbstract() || getEffectiveStatements(method.getBody()).size() == 1);
    }

    public static boolean isInSetter(CtElement ctElement) {
        CtMethod<?> parent = ctElement.getParent(CtMethod.class);
        return parent != null && SpoonUtil.isSetter(parent);
    }

    public static boolean isPrimitiveNumeric(CtTypeReference<?> type) {
        return type.isPrimitive()
               && !type.getQualifiedName().equals("boolean")
               && !type.getQualifiedName().equals("char");
    }

    public static boolean isSignatureEqualTo(
        CtExecutableReference<?> ctExecutableReference,
        Class<?> returnType,
        String methodName,
        Class<?>... parameterTypes
    ) {
        TypeFactory factory = ctExecutableReference.getFactory().Type();
        return SpoonUtil.isSignatureEqualTo(
            ctExecutableReference,
            factory.createReference(returnType),
            methodName,
            Arrays.stream(parameterTypes).map(factory::createReference).toArray(CtTypeReference[]::new)
        );
    }

    public static boolean isSignatureEqualTo(
        CtExecutableReference<?> ctExecutableReference,
        CtTypeReference<?> returnType,
        String methodName,
        CtTypeReference<?>... parameterTypes
    ) {
        // check that they both return the same type
        if (!SpoonUtil.isTypeEqualTo(ctExecutableReference.getType(), returnType)) {
            return false;
        }

        // their names should match:
        if (!ctExecutableReference.getSimpleName().equals(methodName)) {
            return false;
        }

        List<CtTypeReference<?>> givenParameters = ctExecutableReference.getParameters();

        // the number of parameters should match
        if (givenParameters.size() != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            // check if the type of the parameter is equal to the expected type
            if (!SpoonUtil.isTypeEqualTo(givenParameters.get(i), parameterTypes[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a static invocation of the given method on the given target type.
     *
     * @param targetType the type on which the method is defined
     * @param methodName the name of the method
     * @param parameters the parameters to pass to the method
     * @return the invocation
     * @param <T> the result type of the invocation
     */
    public static <T> CtInvocation<T> createStaticInvocation(
        CtTypeReference<?> targetType,
        String methodName,
        CtExpression<?>... parameters
    ) {
        Factory factory = targetType.getFactory();

        CtMethod<T> methodHandle = null;
        List<CtMethod<?>> potentialMethods = targetType.getTypeDeclaration().getMethodsByName(methodName);
        if (potentialMethods.size() == 1) {
            methodHandle = (CtMethod<T>) potentialMethods.get(0);
        } else {
            methodHandle = targetType.getTypeDeclaration().getMethod(
                methodName,
                Arrays.stream(parameters).map(SpoonUtil::getExpressionType).toArray(CtTypeReference[]::new)
            );
        }

        return factory.createInvocation(
            factory.createTypeAccess(methodHandle.getDeclaringType().getReference()),
            methodHandle.getReference(),
            parameters
        );
    }

    public static <T> CtExpression<T> castExpression(Class<T> targetType, CtExpression<?> ctExpression) {
        return SpoonUtil.castExpression(ctExpression.getFactory().Type().createReference(targetType), ctExpression);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T, R> CtLiteral<R> castLiteral(CtTypeReference<R> type, CtLiteral<T> literal) {
        CtLiteral result = literal.clone();
        result.setType(type.clone());

        // casting a primitive to a string:
        if (SpoonUtil.isTypeEqualTo(type, String.class) && literal.getType().isPrimitive()) {
            result.setValue(literal.getValue().toString());
            return result;
        }

        // It is not possible to cast an Integer to a Double directly, which is a problem.
        CtTypeReference<?> targetType = type.unbox();
        if (targetType.isPrimitive()) {
            // the FoldUtils.convert method only works for Number -> Number conversions
            if (targetType.box().isSubtypeOf(type.getFactory().createCtTypeReference(Number.class))) {
                // for instances of Number, one can use the convert method:
                if (literal.getValue() instanceof Number number) {
                    result.setValue(FoldUtils.convert(type, number));
                } else {
                    // primitive types that do not implement Number are:
                    // boolean, char

                    // NOTE: it does not make sense to cast a boolean to any other primitive type
                    if (literal.getValue() instanceof Character character) {
                        result.setValue(FoldUtils.convert(type, (int) character));
                    }
                }
            }

            if (SpoonUtil.isTypeEqualTo(targetType, char.class)) {
                if (literal.getValue() instanceof Number number) {
                    result.setValue((char) number.intValue());
                } else {
                    result.setValue((char) literal.getValue());
                }
            } else if (SpoonUtil.isTypeEqualTo(targetType, boolean.class)) {
                result.setValue((boolean) literal.getValue());
            }
        } else {
            result.setValue(type.getActualClass().cast(literal.getValue()));
        }

        return result;
    }

    // returns the type of the expression after applying the type casts
    public static <T> CtTypeReference<?> getExpressionType(CtExpression<T> ctExpression) {
        CtTypeReference<?> result = ctExpression.getType();

        List<CtTypeReference<?>> typeCasts = ctExpression.getTypeCasts();
        if (!typeCasts.isEmpty()) {
            result = typeCasts.get(0);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T, E extends CtExpression<T>> E castExpression(CtTypeReference<T> type, CtExpression<?> ctExpression) {
        // no need to cast if the type is the same
        if (SpoonUtil.getExpressionType(ctExpression).equals(type)) {
            return (E) ctExpression;
        }

        List<CtTypeReference<?>> typeCasts = new ArrayList<>(ctExpression.getTypeCasts());
        typeCasts.add(0, type.clone());
        ctExpression.setTypeCasts(typeCasts);

        return (E) RemoveRedundantCasts.removeRedundantCasts(ctExpression);
    }

    public static Optional<CtJavaDoc> getJavadoc(CtElement element) {
        if (element.getComments().isEmpty() || !(element.getComments().get(0) instanceof CtJavaDoc)) {
            // TODO lookup inherited javadoc
            return Optional.empty();
        } else {
            return Optional.of(element.getComments().get(0).asJavaDoc());
        }
    }

    public static boolean isStaticCallTo(CtInvocation<?> invocation, String typeName, String methodName) {
        return invocation.getExecutable().isStatic()
               && invocation.getTarget() instanceof CtTypeAccess<?> access
               && access.getAccessedType().getQualifiedName().equals(typeName)
               && invocation.getExecutable().getSimpleName().equals(methodName);
    }

    public static boolean isEffectivelyFinal(CtVariable<?> ctVariable) {
        if (ctVariable.getModifiers().contains(ModifierKind.FINAL)) {
            return true;
        }
        return !CodeModel.getFor(ctVariable).getUses().hasAnyUses(ctVariable, use -> use instanceof CtVariableWrite<?>);
    }

    public static <T> Optional<CtExpression<T>> getEffectivelyFinalExpression(CtVariable<T> ctVariable) {
        if (!isEffectivelyFinal(ctVariable)) {
            return Optional.empty();
        }

        return Optional.ofNullable(ctVariable.getDefaultExpression());
    }

    /**
     * Checks if the given element is guaranteed to be immutable.
     * <p>
     * Note that when this method returns {@code false}, the type might still be immutable.
     *
     * @param ctTypeReference the type to check
     * @return true if the given element is guaranteed to be immutable, false otherwise
     * @param <T> the type of the element
     */
    public static <T> boolean isImmutable(CtTypeReference<T> ctTypeReference) {
        Deque<CtTypeReference<?>> queue = new ArrayDeque<>(Collections.singletonList(ctTypeReference));
        Collection<CtType<?>> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            CtType<?> ctType = queue.removeFirst().getTypeDeclaration();

            // if the type is not in the classpath, null is returned
            // in those cases, assume that the type is not immutable
            if (ctType == null) {
                return false;
            }

            // skip types that have been checked (those are guaranteed to be immutable)
            if (visited.contains(ctType)) {
                continue;
            }

            // primitive types and strings are immutable as well:
            if (ctType.getReference().unbox().isPrimitive()
                || SpoonUtil.isTypeEqualTo(ctType.getReference(), java.lang.String.class)) {
                continue;
            }

            // types that are not in the classpath like java.util.ArrayList are shadow types.
            // the source code for those is missing, so it is impossible to check if they are immutable.
            // => assume they are not immutable
            if (ctType.isShadow()) {
                return false;
            }

            // for a type to be immutable, all of its fields must be final and immutable as well:
            for (CtFieldReference<?> ctFieldReference : ctType.getAllFields()) {
                if (!SpoonUtil.isEffectivelyFinal(ctFieldReference.getFieldDeclaration())) {
                    return false;
                }

                queue.add(ctFieldReference.getType());
            }

            visited.add(ctType);
        }

        return true;
    }

    /**
     * Checks if the given type is equal to any of the expected types.
     *
     * @param ctType the type to check
     * @param expected all allowed types
     * @return true if the given type is equal to any of the expected types, false otherwise
     */
    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, Class<?>... expected) {
        TypeFactory factory = ctType.getFactory().Type();
        return SpoonUtil.isTypeEqualTo(
            ctType,
            Arrays.stream(expected)
                .map(factory::createReference)
                .toArray(CtTypeReference[]::new)
        );
    }

    /**
     * Checks if the given type is equal to any of the expected types.
     *
     * @param ctType the type to check
     * @param expected all allowed types
     * @return true if the given type is equal to any of the expected types, false otherwise
     */
    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, CtTypeReference<?>... expected) {
        return Arrays.asList(expected).contains(ctType);
    }

    public static boolean isSubtypeOf(CtTypeReference<?> ctTypeReference, Class<?> expected) {
        // NOTE: calling isSubtypeOf on CtTypeParameterReference will result in a crash
        return !(ctTypeReference instanceof CtTypeParameterReference)
            && ctTypeReference.isSubtypeOf(ctTypeReference.getFactory().Type().createReference(expected));
    }

    public static boolean isMainMethod(CtMethod<?> method) {
        return method.isStatic()
            && method.isPublic()
            && SpoonUtil.isSignatureEqualTo(
                method.getReference(),
                void.class,
                "main",
                java.lang.String[].class
            );
    }

    /**
     * Returns an iterable over all parents of the given element.
     *
     * @param ctElement the element to get the parents of
     * @return an iterable over all parents, the given element is not included
     */
    private static Iterable<CtElement> parents(CtElement ctElement) {
        return () -> new Iterator<>() {
            private CtElement current = ctElement;

            @Override
            public boolean hasNext() {
                return this.current.isParentInitialized();
            }

            @Override
            public CtElement next() throws NoSuchElementException {
                if (!this.hasNext()) {
                    throw new NoSuchElementException("No more parents");
                }

                CtElement result = this.current.getParent();
                this.current = result;
                return result;
            }
        };
    }

    private static <T, E> HashSet<T> newHashSet(Iterator<? extends E> elements, Function<E, ? extends T> mapper) {
        HashSet<T> set = new HashSet<>();
        for (; elements.hasNext(); set.add(mapper.apply(elements.next())));
        return set;
    }

    private record IdentityKey<T>(T value) {
        public static <T> IdentityKey<T> of(T value) {
            return new IdentityKey<>(value);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            IdentityKey<?> that = (IdentityKey<?>) obj;
            return this.value == that.value();
        }
    }

    public static void visitCtCompilationUnit(CtModel ctModel, Consumer<? super CtCompilationUnit> lambda) {
        // it is not possible to visit CtCompilationUnit through the processor API.
        //
        // in https://github.com/INRIA/spoon/issues/5168 the below code is mentioned as a workaround:
        ctModel
            .getAllTypes()
            .stream()
            .map(CtType::getPosition)
            .filter(SourcePosition::isValidPosition)
            .map(SourcePosition::getCompilationUnit)
            // visit each compilation unit only once
            .distinct()
            .forEach(lambda);
    }

    /**
     * Finds the closest common parent of the given elements.
     *
     * @param firstElement the first element to find the common parent of
     * @param others any amount of other elements to find the common parent of
     * @return the closest common parent of the given elements or the firstElement itself if others is empty
     */
    public static CtElement findCommonParent(CtElement firstElement, Iterable<? extends CtElement> others) {
        // CtElement::hasParent will recursively call itself until it reaches the root
        // => inefficient and might cause a stack overflow

        // add all parents of the firstElement to a set sorted by distance to the firstElement:
        Set<IdentityKey<CtElement>> ctParents = new LinkedHashSet<>();
        ctParents.add(IdentityKey.of(firstElement));
        parents(firstElement).forEach(element -> ctParents.add(IdentityKey.of(element)));

        for (CtElement other : others) {
            // only keep the parents that the firstElement and the other have in common
            ctParents.retainAll(newHashSet(parents(other).iterator(), IdentityKey::of));
        }

        // the first element in the set is the closest common parent
        return ctParents.iterator().next().value();
    }

    /**
     * Checks if the given type is an inner class.
     *
     * @param type the type to check, not null
     * @return true if the given type is an inner class, false otherwise
     */
    public static boolean isInnerClass(CtTypeMember type) {
        return type.getDeclaringType() != null;
    }

    public static boolean isInOverridingMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return CodeModel.getFor(ctElement).getMethodHierarchy().isOverridingMethod(ctMethod);
    }

    /**
     * Checks if the given method is an invocation.
     * @param statement which is checked
     * @return true if the statement is an invocation (instance of CtInvocation, CtConstructorCall or CtLambda),
     * false otherwise
     */
    public static boolean isInvocation(CtStatement statement) {
        return statement instanceof CtInvocation<?> || statement instanceof CtConstructorCall<?> ||
                statement instanceof CtLambda<?>;
    }

    public static boolean isInMainMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return isMainMethod(ctMethod);
    }


    public static CtElement getReferenceDeclaration(CtReference ctReference) {
        // this might be null if the reference is not in the source path
        // for example, when the reference points to a java.lang type
        CtElement target = ctReference.getDeclaration();

        if (target == null && ctReference instanceof CtTypeReference<?> ctTypeReference) {
            target = ctTypeReference.getTypeDeclaration();
        }

        if (target == null && ctReference instanceof CtExecutableReference<?> ctExecutableReference) {
            target = ctExecutableReference.getExecutableDeclaration();
        }

        if (target == null && ctReference instanceof CtFieldReference<?> ctFieldReference) {
            target = ctFieldReference.getFieldDeclaration();
        }

        if (target == null && ctReference instanceof CtLocalVariableReference<?> ctLocalVariableReference) {
            target = getLocalVariableDeclaration(ctLocalVariableReference);
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    private static <T> CtLocalVariable<T> getLocalVariableDeclaration(CtLocalVariableReference<T> ctLocalVariableReference) {
        if (ctLocalVariableReference.getDeclaration() != null) {
            return ctLocalVariableReference.getDeclaration();
        }

        // handle the special case, where we have an instanceof Pattern:
        for (CtElement parent : parents(ctLocalVariableReference)) {
            CtLocalVariable<?> candidate = parent.filterChildren(new TypeFilter<>(CtTypePattern.class)).filterChildren(new CompositeFilter<>(
                FilteringOperator.INTERSECTION,
                new TypeFilter<>(CtLocalVariable.class),
                element -> element.getReference().equals(ctLocalVariableReference)
            )).first();

            if (candidate != null) {
                return (CtLocalVariable<T>) candidate;
            }
        }

        return null;
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
        if (ctStatement.getParent() instanceof CtStatementList ctStatementList) {
            List<CtStatement> statements = ctStatementList.getStatements();
            int index = referenceIndexOf(statements, ctStatement);

            if (index > 0) {
                return Optional.of(statements.get(index - 1));
            }
        }

        return Optional.empty();
    }

    public static List<CtStatement> getNextStatements(CtStatement ctStatement) {
        List<CtStatement> result = new ArrayList<>();
        if (ctStatement.getParent() instanceof CtStatementList ctStatementList) {
            List<CtStatement> statements = ctStatementList.getStatements();
            int index = referenceIndexOf(statements, ctStatement);

            if (index > 0) {
                result.addAll(statements.subList(index + 1, statements.size()));
            }
        }

        return result;
    }

    public static String truncatedSuggestion(CtElement ctElement) {
        StringJoiner result = new StringJoiner(System.lineSeparator());

        for (String line : ctElement.toString().split("\\r?\\n")) {
            int newLineLength = 0;

            // this ensures that the truncation is the same on linux and windows
            if (!result.toString().contains("\r\n")) {
                newLineLength += (int) result.toString().chars().filter(ch -> ch == '\n').count();
            }

            if (result.length() + newLineLength > 150) {
                if (line.startsWith(" ")) {
                    result.add("...".indent(line.length() - line.stripIndent().length()).stripTrailing());
                } else {
                    result.add("...");
                }

                if (result.toString().startsWith("{")) {
                    result.add("}");
                }

                break;
            }

            result.add(line);
        }

        return result.toString();
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

    public static List<Effect> getCasesEffects(Iterable<? extends CtCase<?>> ctCases) {
        List<Effect> effects = new ArrayList<>();
        for (CtCase<?> ctCase : ctCases) {
            Optional<Effect> effect = SpoonUtil.getSingleEffect(ctCase.getStatements());
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

    public static SourcePosition findPosition(CtElement ctElement) {
        if (ctElement.getPosition().isValidPosition()) {
            return ctElement.getPosition();
        }

        for (CtElement element : parents(ctElement)) {
            if (element.getPosition().isValidPosition()) {
                return element.getPosition();
            }
        }

        return null;
    }

    /**
     * Converts the provided source position into a human-readable string.
     *
     * @param sourcePosition the source position as given by spoon
     * @return a human-readable string representation of the source position
     */
    public static String formatSourcePosition(SourcePosition sourcePosition) {
        return String.format("%s:L%d", getBaseName(sourcePosition.getFile().getName()), sourcePosition.getLine());
    }

    private static String getBaseName(String fileName) {
        if (fileName == null) {
            return null;
        }
        return FilenameUtils.removeExtension(new File(fileName).getName());
    }

    public static SourcePosition getNamePosition(CtNamedElement ctNamedElement) {
        SourcePosition position = ctNamedElement.getPosition();

        if (position instanceof CompoundSourcePosition compoundSourcePosition) {
            return ctNamedElement.getFactory().createSourcePosition(
                position.getCompilationUnit(),
                compoundSourcePosition.getNameStart(),
                compoundSourcePosition.getNameEnd(),
                position.getCompilationUnit().getLineSeparatorPositions()
            );
        }

        return position;
    }

    public static <P extends CtElement> P getParentOrSelf(CtElement element, Class<P> parentType) {
        Objects.requireNonNull(element);
        if (parentType.isAssignableFrom(element.getClass())) {
            return (P) element;
        }
        return element.getParent(parentType);
    }

    public static int getParameterIndex(CtParameter<?> parameter, CtExecutable<?> executable) {
        for (int i = 0; i < executable.getParameters().size(); i++) {
            if (executable.getParameters().get(i) == parameter) {
                return i;
            }
        }
        throw new IllegalArgumentException("Parameter not found in executable");
    }
}
