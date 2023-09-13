package de.firemage.autograder.core.integrated;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import de.firemage.autograder.core.integrated.effects.AssignmentStatement;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import de.firemage.autograder.core.integrated.effects.TerminalStatement;
import de.firemage.autograder.core.integrated.evaluator.Evaluator;
import de.firemage.autograder.core.integrated.evaluator.fold.FoldUtils;
import de.firemage.autograder.core.integrated.evaluator.fold.InferOperatorTypes;
import de.firemage.autograder.core.integrated.evaluator.fold.InlineVariableRead;
import de.firemage.autograder.core.integrated.evaluator.fold.RemoveRedundantCasts;
import org.apache.commons.compress.utils.FileNameUtils;
import spoon.reflect.CtModel;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtJavaDoc;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.LiteralBase;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.CompoundSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.eval.PartialEvaluator;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.DirectReferenceFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.OverridingMethodFilter;
import spoon.reflect.visitor.filter.SameFilter;
import spoon.reflect.visitor.filter.VariableAccessFilter;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpoonUtil {

    private SpoonUtil() {

    }

    public static boolean isString(CtTypeReference<?> type) {
        return isTypeEqualTo(type, java.lang.String.class);
    }

    public static Optional<CtTypeReference<?>> isToStringCall(CtExpression<?> expression) {
        if (!SpoonUtil.isString(expression.getType())) {
            return Optional.empty();
        }

        if (expression instanceof CtInvocation<?> invocation &&
            invocation.getExecutable().getSignature().equals("toString()")) {
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

    public static <T> CtLiteral<T> makeLiteral(T value) {
        CtLiteral<T> literal = new CtLiteralImpl<>();
        literal.setValue(value);
        return literal;
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

    /**
     * Returns the default value of the given type.
     *
     * @param ty  a reference to the type
     * @param <T> the type of the value
     *
     * @return the default value of the given type
     */
    @SuppressWarnings("unchecked")
    public static <T> CtLiteral<T> getDefaultValue(CtTypeReference<T> ty) {
        if (ty.isPrimitive()) {
            return (CtLiteral<T>) Map.ofEntries(
                Map.entry("int", makeLiteral(0)),
                Map.entry("double", makeLiteral(0.0d)),
                Map.entry("float", makeLiteral(0.0f)),
                Map.entry("long", makeLiteral(0L)),
                Map.entry("short", makeLiteral((short) 0)),
                Map.entry("byte", makeLiteral((byte) 0)),
                Map.entry("char", makeLiteral((char) 0)),
                Map.entry("boolean", makeLiteral(false))
            ).get(ty.getSimpleName());
        } else {
            return makeLiteral(null);
        }
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

    /**
     * Returns the variable from the array access. For example array[0][1] will return array.
     *
     * @param ctArrayAccess the array access
     * @return the variable
     */
    public static CtVariableAccess<?> getVariableFromArray(CtArrayAccess<?, ?> ctArrayAccess) {
        CtExpression<?> array = ctArrayAccess.getTarget();

        if (array instanceof CtVariableAccess<?>) {
            return (CtVariableAccess<?>) array;
        } else if (array instanceof CtArrayAccess<?, ?> access) {
            return getVariableFromArray(access);
        } else {
            throw new IllegalArgumentException("Unable to obtain variable from array access: " + ctArrayAccess);
        }
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
            step.setType(ctBinaryOperator.getFactory().Type().CHARACTER_PRIMITIVE);
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
            leftHandOperand,
            rightHandOperand,
            operatorKind
        );

        if (ctBinaryOperator.getType() == null) {
            ctBinaryOperator.setType(FoldUtils.inferType(ctBinaryOperator));
        }

        return ctBinaryOperator;
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static <T> CtUnaryOperator<T> createCtUnaryOperator(UnaryOperatorKind operatorKind, CtExpression<?> ctExpression) {
        CtUnaryOperator ctUnaryOperator = ctExpression.getFactory().createUnaryOperator();
        ctUnaryOperator.setOperand(ctExpression);
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
     * Converts a binary operator like < to <= or > to >= and adjusts the operands accordingly
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
                case NE -> {
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

        return createCtUnaryOperator(UnaryOperatorKind.NOT, ctExpression.clone());
    }

    public static List<CtStatement> getEffectiveStatements(CtStatement ctStatement) {
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

    public static boolean isPrimitiveNumeric(CtTypeReference<?> type) {
        return type.isPrimitive()
               && !type.getQualifiedName().equals("boolean")
               && !type.getQualifiedName().equals("char");
    }

    public static boolean isVoidMethod(CtMethod<?> method) {
        return method.getType().getQualifiedName().equals("void");
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
        return SpoonUtil.isTypeEqualTo(ctExecutableReference.getType(), returnType)
            // their names should match:
            && ctExecutableReference.getSimpleName().equals(methodName)
            // the number of parameters should match
            && ctExecutableReference.getParameters().size() == parameterTypes.length
            && Streams.zip(
                // combine both the parameters of the executable and the expected types
                ctExecutableReference.getParameters().stream(),
                Arrays.stream(parameterTypes),
                // evaluate if the type of the parameter is equal to the expected type:
                SpoonUtil::isTypeEqualTo
                // On this stream of booleans, check if all are true
            ).allMatch(value -> value);
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
        CtMethod<T> methodHandle = targetType.getTypeDeclaration().getMethod(
            methodName,
            Arrays.stream(parameters).map(SpoonUtil::getExpressionType).toArray(CtTypeReference[]::new)
        );

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

    public static boolean isEffectivelyFinal(CtVariableReference<?> ctVariableReference) {
        return isEffectivelyFinal(ctVariableReference.getFactory().getModel(), ctVariableReference);
    }

    public static boolean isEffectivelyFinal(CtModel ctModel, CtVariableReference<?> ctVariableReference) {
        return ctVariableReference.getModifiers().contains(ModifierKind.FINAL) || ctModel
                .filterChildren(e -> e instanceof CtVariableWrite<?> write &&
                        write.getVariable().equals(ctVariableReference))
                .first() == null;
    }

    public static <T> Optional<CtExpression<T>> getEffectivelyFinalExpression(CtVariableReference<T> ctVariableReference) {
        if (!isEffectivelyFinal(ctVariableReference)) {
            return Optional.empty();
        }

        if (ctVariableReference.getDeclaration() == null) {
            // this pointer
            return Optional.empty();
        }

        return Optional.ofNullable(ctVariableReference.getDeclaration().getDefaultExpression());
    }

    public static <T> boolean isImmutable(CtTypeReference<T> ctTypeReference) {
        CtType<T> ctType = ctTypeReference.getTypeDeclaration();
        if (ctType == null) {
            return false;
        }

        if (ctTypeReference.unbox().isPrimitive() || SpoonUtil.isTypeEqualTo(ctTypeReference, java.lang.String.class)) {
            return true;
        }

        if (ctType.isShadow()) {
            return false;
        }

        return ctType.getAllFields().stream()
            .allMatch(ctFieldReference -> SpoonUtil.isEffectivelyFinal(ctFieldReference)
                && SpoonUtil.isImmutable(ctFieldReference.getType()));
    }

    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, Class<?>... expected) {
        TypeFactory factory = ctType.getFactory().Type();
        return SpoonUtil.isTypeEqualTo(
            ctType,
            Arrays.stream(expected)
                .map(factory::createReference)
                .toArray(CtTypeReference[]::new)
        );
    }

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
    public static Iterable<CtElement> parents(CtElement ctElement) {
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
        Set<CtElement> ctParents = new LinkedHashSet<>();
        ctParents.add(firstElement);
        parents(firstElement).forEach(ctParents::add);

        for (CtElement other : others) {
            // only keep the parents that the firstElement and the other have in common
            ctParents.retainAll(Sets.newHashSet(parents(other).iterator()));
        }

        // the first element in the set is the closest common parent
        return ctParents.iterator().next();
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

    public static boolean isInnerClass(CtTypeReference<?> ctTypeReference) {
        return ctTypeReference.getDeclaringType() != null;
    }

    /**
     * Checks if the given method is overriding another method.
     * <p>
     * This implies that there is another method in a super class or interface that has the same signature.
     *
     * @param ctMethod the method to check, must not be null
     * @return true if the given method is overriding another method, false otherwise
     */
    public static boolean isOverriddenMethod(CtMethod<?> ctMethod) {
        Collection<CtMethod<?>> topDefinitions = ctMethod.getTopDefinitions();
        // if the method is defined for the first time, this should return an empty collection
        return !topDefinitions.isEmpty();
        // TODO: this method is kinda whack
    }

    public static boolean isInOverriddenMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return isOverriddenMethod(ctMethod);
    }

    public static boolean isInMainMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return isMainMethod(ctMethod);
    }

    /**
     * Finds all uses of {@code ctElement} in {@code in}.
     *
     * @param ctElement the element to search for
     * @param in the element to search in
     * @return all uses of {@code ctElement} in {@code in}
     */
    public static List<CtElement> findUsesIn(CtElement ctElement, CtElement in) {
        return findUses(ctElement).stream()
            .filter(element -> !in.getElements(new SameFilter(element)).isEmpty())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public record FilterAdapter<T extends CtElement>(Filter<T> filter, Class<T> type) implements Filter<CtElement> {
        @Override
        public boolean matches(CtElement element) {
            if (this.type.isInstance(element)) {
                return this.filter.matches(this.type.cast(element));
            }

            return false;
        }
    }

    private static final Filter<CtElement> EXPLICIT_ELEMENT_FILTER = ctElement -> !ctElement.isImplicit();

    /**
     * This class is like {@link VariableAccessFilter}, but does work with generics.
     *
     * @param ctVariable the variable to find accesses to
     * @param <T> the type of accesses to find
     * @see <a href="https://github.com/INRIA/spoon/issues/5391">INRIA/spoon/issues/5391</a>
     */
    private record BetterVariableAccessFilter<T extends CtVariableAccess<?>>(CtVariable<?> ctVariable) implements Filter<T> {
        @Override
        public boolean matches(T element) {
            return element.getVariable().equals(this.ctVariable.getReference())
                || (element.getVariable().getDeclaration() != null && element.getVariable().getDeclaration().equals(this.ctVariable));
        }
    }

    private record BetterInvocationFilter(CtExecutable<?> executable) implements Filter<CtAbstractInvocation<?>> {
        @Override
        public boolean matches(CtAbstractInvocation<?> invocation) {
            CtExecutableReference<?> invocationExecutable = invocation.getExecutable();
            return invocationExecutable.equals(this.executable.getReference())
                || this.executable.equals(invocationExecutable.getExecutableDeclaration())
                // TODO: consider removing this?
                || invocationExecutable.isOverriding(this.executable.getReference());
        }
    }

    public static class UsesFilter implements Filter<CtElement> {
        private final Filter<CtElement> filter;

        @SuppressWarnings("unchecked")
        public UsesFilter(CtElement ctElement) {
            Filter<CtElement> filter;

            if (ctElement instanceof CtVariable<?> ctVariable) {
                filter = new FilterAdapter<>(new BetterVariableAccessFilter<>(ctVariable), CtVariableAccess.class);
            } else if (ctElement instanceof CtExecutable<?> ctExecutable) {
                filter = buildExecutableFilter(ctExecutable);
            } else if (ctElement instanceof CtTypeMember ctTypeMember) {
                // CtTypeMember that are not executable or variables are:
                // - CtType (CtClass, CtEnum, CtInterface, CtRecord)
                // - CtFormalTypeDeclarer (CtTypeParameter)
                filter = new FilterAdapter<>(
                    new DirectReferenceFilter<>(ctTypeMember.getReference()),
                    CtReference.class
                );
            } else {
                throw new IllegalArgumentException("Unsupported element: " + ctElement.getClass().getName());
            }

            // only consider explicit elements
            this.filter = new CompositeFilter<>(FilteringOperator.INTERSECTION, EXPLICIT_ELEMENT_FILTER, filter);
        }

        @Override
        public boolean matches(CtElement element) {
            return this.filter.matches(element);
        }

        @SuppressWarnings("unchecked")
        private static Filter<CtElement> buildExecutableFilter(CtExecutable<?> ctExecutable) {
            Filter<CtElement> filter = new FilterAdapter<>(
                new BetterInvocationFilter(ctExecutable),
                // CtAbstractInvocation.class => Class<CtAbstractInvocation>, but Class<CtAbstractInvocation<?>> is needed
                (Class<CtAbstractInvocation<?>>) (Object) CtAbstractInvocation.class
            );

            if (ctExecutable instanceof CtMethod<?> ctMethod) {
                // implementing an abstract method is considered a use:
                filter = new CompositeFilter<>(
                    FilteringOperator.UNION,
                    filter,
                    new FilterAdapter<>(
                        // this filter finds all methods that override the given method
                        new OverridingMethodFilter(ctMethod),
                        (Class<CtMethod<?>>) (Object) CtMethod.class
                    )
                );
            }

            return filter;
        }
    }

    public static List<CtElement> findUses(CtElement ctElement) {
        return new ArrayList<>(ctElement.getFactory().getModel().getElements(new UsesFilter(ctElement)));
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

    /**
     * Converts the provided source position into a human-readable string.
     *
     * @param sourcePosition the source position as given by spoon
     * @return a human-readable string representation of the source position
     */
    public static String formatSourcePosition(SourcePosition sourcePosition) {
        return String.format("%s:L%d", FileNameUtils.getBaseName(sourcePosition.getFile().getName()), sourcePosition.getLine());
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
}
