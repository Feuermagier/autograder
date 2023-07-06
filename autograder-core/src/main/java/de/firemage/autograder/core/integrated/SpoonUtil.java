package de.firemage.autograder.core.integrated;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import de.firemage.autograder.core.integrated.effects.AssignmentStatement;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import de.firemage.autograder.core.integrated.effects.TerminalStatement;
import org.apache.commons.compress.utils.FileNameUtils;
import spoon.reflect.CtModel;
import spoon.reflect.code.BinaryOperatorKind;
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
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.LiteralBase;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.eval.PartialEvaluator;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.CtVisitor;
import spoon.reflect.visitor.filter.OverridingMethodFilter;
import spoon.support.reflect.code.CtLiteralImpl;

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

    public static boolean isNullLiteral(CtExpression<?> expression) {
        return SpoonUtil.resolveCtExpression(expression) instanceof CtLiteral<?> literal && literal.getValue() == null;
    }

    public static boolean isIntegerLiteral(CtExpression<?> expression, int value) {
        return expression instanceof CtLiteral<?> literal && literal.getValue().equals(value);
    }

    public static boolean isStringLiteral(CtExpression<?> expression, String value) {
        return expression instanceof CtLiteral<?> literal && literal.getValue() != null &&
               literal.getValue().equals(value);
    }

    public static boolean isBoolean(CtTypedElement<?> ctTypedElement) {
        CtTypeReference<?> ctTypeReference = ctTypedElement.getType();
        return ctTypeReference != null
            && (SpoonUtil.isTypeEqualTo(ctTypeReference, boolean.class) || SpoonUtil.isTypeEqualTo(
            ctTypeReference,
            Boolean.class
        ));
    }

    public static Optional<Boolean> tryGetBooleanLiteral(CtExpression<?> expression) {
        if (SpoonUtil.resolveCtExpression(expression) instanceof CtLiteral<?> literal
            && literal.getValue() != null
            && isBoolean(literal)) {

            return Optional.of((Boolean) literal.getValue());
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> tryGetStringLiteral(CtExpression<?> expression) {
        if (SpoonUtil.resolveCtExpression(expression) instanceof CtLiteral<?> literal
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

    /**
     * Makes a new literal with the given value.
     *
     * @param value the value of the literal
     * @param <T>   the type of the value
     *
     * @return a new literal with the given value, note that the base is not set
     */
    public static <T> CtLiteral<T> makeLiteral(T value) {
        CtLiteral<T> literal = new CtLiteralImpl<>();
        literal.setValue(value);
        return literal;
    }

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
     * Copy-pasted from {@link spoon.support.reflect.eval.VisitorPartialEvaluator}.
     *
     * @param type the type of the number
     * @param number some number that should be converted to a value of the given type
     * @return the converted number
     */
    private static Number convert(CtTypeReference<?> type, Number number) {
        if ((type.getActualClass() == int.class) || (type.getActualClass() == Integer.class)) {
            return number.intValue();
        }
        if ((type.getActualClass() == byte.class) || (type.getActualClass() == Byte.class)) {
            return number.byteValue();
        }
        if ((type.getActualClass() == long.class) || (type.getActualClass() == Long.class)) {
            return number.longValue();
        }
        if ((type.getActualClass() == float.class) || (type.getActualClass() == Float.class)) {
            return number.floatValue();
        }
        if ((type.getActualClass() == short.class) || (type.getActualClass() == Short.class)) {
            return number.shortValue();
        }
        if ((type.getActualClass() == double.class) || (type.getActualClass() == Double.class)) {
            return number.doubleValue();
        }
        return number;
    }

    private static <T> CtExpression<?> partiallyEvaluate(CtExpression<T> ctExpression) {
        // NOTE: this is a workaround for https://github.com/INRIA/spoon/issues/5273
        PartialEvaluator evaluator = new VisitorEvaluator();
        CtExpression<?> res = evaluator.evaluate(ctExpression).clone();

        // ensure that all literals have a type (workaround for broken PartialEvaluator implementation)
        CtVisitor ctVisitor = new VisitorCtLiteralTypeFixer(ctExpression.getFactory());
        res.accept(ctVisitor);

        return res;
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
            step.setValue(1);
            step.setType(ctBinaryOperator.getFactory().Type().CHARACTER_PRIMITIVE);
        } else {
            // this assumes that < and > are only used with numbers
            step.setValue(convert(ctBinaryOperator.getRightHandOperand().getType(), ((Number) 1).doubleValue()));
            step.setType(ctBinaryOperator.getRightHandOperand().getType());
        }

        CtBinaryOperator<T> result = ctBinaryOperator.clone();
        if (ctBinaryOperator.getKind() == BinaryOperatorKind.LT) {
            // <lhs> < <rhs> => <lhs> <= <rhs> - 1
            result.setKind(BinaryOperatorKind.LE);
            result.setRightHandOperand(ctBinaryOperator.getFactory().createBinaryOperator(
                ctBinaryOperator.getRightHandOperand(),
                step,
                BinaryOperatorKind.MINUS
            ));
        } else if (ctBinaryOperator.getKind() == BinaryOperatorKind.GT) {
            // <lhs> > <rhs> => <lhs> >= <rhs> + 1
            result.setKind(BinaryOperatorKind.GE);
            result.setRightHandOperand(ctBinaryOperator.getFactory().createBinaryOperator(
                ctBinaryOperator.getRightHandOperand(),
                step,
                BinaryOperatorKind.PLUS
            ));
        }

        // TODO: this should call the SpoonUtil.partiallyEvaluate
        PartialEvaluator evaluator = new VisitorEvaluator();
        CtBinaryOperator<T> res = evaluator.evaluate(result);

        // ensure that all literals have a type (workaround for broken PartialEvaluator implementation)
        CtVisitor ctVisitor = new VisitorCtLiteralTypeFixer(ctBinaryOperator.getFactory());
        res.accept(ctVisitor);

        return res;
    }

    /**
     * Swaps the operands of a binary operator.
     *
     * @param ctBinaryOperator the operator to swap, can be of any kind
     * @return the cloned version with the operands swapped or the given operator if it is not supported
     * @param <T> the type the operator evaluates to
     */
    public static <T> CtBinaryOperator<T> swapCtBinaryOperator(CtBinaryOperator<T> ctBinaryOperator) {
        CtBinaryOperator result = ctBinaryOperator.clone();

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
        CtExpression<?> tmp = result.getLeftHandOperand();
        result.setLeftHandOperand(result.getRightHandOperand());
        result.setRightHandOperand(tmp);

        return result;
    }

    /**
     * Converts a binary operator like < to <= or > to >= and adjusts the operands accordingly
     * to make finding patterns on them easier by not having to special-case them. Additionally
     * one can specify a predicate to swap the operands if necessary. For example to ensure that
     * a literal is always on the right hand side.
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
        CtExpression<?> left = SpoonUtil.resolveCtExpression(ctBinaryOperator.getLeftHandOperand());
        CtExpression<?> right = SpoonUtil.resolveCtExpression(ctBinaryOperator.getRightHandOperand());
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
                // !(a || b) -> !a && !bS
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

        CtUnaryOperator ctUnaryOperator = ctExpression.getFactory().createUnaryOperator();

        ctUnaryOperator.setKind(UnaryOperatorKind.NOT);
        ctUnaryOperator.setOperand(ctExpression.clone());

        return ctUnaryOperator;
    }

    public static List<CtStatement> getEffectiveStatements(CtStatement ctStatement) {
        if (ctStatement instanceof CtStatementList ctStatementList) {
            return getEffectiveStatements(ctStatementList.getStatements());
        }

        return getEffectiveStatements(List.of(ctStatement));
    }

    @SuppressWarnings("unchecked")
    public static <T> CtExpression<T> resolveCtExpression(CtExpression<T> ctExpression) {
        if (ctExpression == null) return null;

        CtModel ctModel = ctExpression.getFactory().getModel();

        // inline constants:
        if (ctExpression instanceof CtVariableRead<?> ctVariableRead) {
            CtVariableReference<?> ctVariableReference = ctVariableRead.getVariable();

            Optional<CtExpression<?>> ctExpressionOptional = SpoonUtil.getEffectivelyFinalExpression(
                ctModel,
                ctVariableReference
            );

            // only inline literals:
            if (ctExpressionOptional.isPresent() && ctExpressionOptional.get() instanceof CtLiteral<?> ctLiteral) {
                return (CtLiteral<T>) ctLiteral;
            }
        }

        if (ctExpression instanceof CtBinaryOperator<?> ctBinaryOperator) {
            CtExpression<?> left = resolveCtExpression(ctBinaryOperator.getLeftHandOperand());
            CtExpression<?> right = resolveCtExpression(ctBinaryOperator.getRightHandOperand());

            if (left instanceof CtLiteral<?> && right instanceof CtLiteral<?>) {
                // TODO: this should be able to handle much more
                return (CtExpression<T>) partiallyEvaluate(ctBinaryOperator.clone());
            }

            // a + 0 or a - 0
            if (right instanceof CtLiteral<?> literal
                && literal.getValue() instanceof Integer integer
                && integer == 0
                && Set.of(BinaryOperatorKind.MINUS, BinaryOperatorKind.PLUS).contains(ctBinaryOperator.getKind())) {
                return (CtExpression<T>) left;
            }

            // 0 + a (0 - a != a)
            if (left instanceof CtLiteral<?> literal
                && literal.getValue() instanceof Integer integer
                && integer == 0
                && ctBinaryOperator.getKind() == BinaryOperatorKind.PLUS) {
                return (CtExpression<T>) right;
            }
        }

        return ctExpression;
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
            Arrays.stream(parameters).map(CtTypedElement::getType).toArray(CtTypeReference[]::new)
        );

        return factory.createInvocation(
            factory.createTypeAccess(methodHandle.getDeclaringType().getReference()),
            methodHandle.getReference(),
            parameters
        );
    }

    private static boolean requiresCast(CtTypedElement<?> ctTypedElement, CtTypeReference<?> targetType) {
        CtTypeReference<?> currentType = ctTypedElement.getType();
        if (currentType.equals(targetType)) {
            return false;
        }

        // implicit boxing/unboxing:
        if (currentType.isPrimitive() && currentType.box().equals(targetType)) {
            return false;
        }

        if (targetType.isPrimitive() && targetType.box().equals(currentType)) {
            return false;
        }

        // NOTE: primitive widening is not implemented yet (e.g. int -> long)

        return true;
    }

    public static <T> CtExpression<T> castExpression(CtExpression<?> ctExpression, Class<?> targetType) {
        return castExpression(ctExpression, ctExpression.getFactory().Type().createReference(targetType));
    }

    public static <T> CtExpression<T> castExpression(CtExpression<?> ctExpression, CtTypeReference<?> targetType) {
        CtExpression result = ctExpression.clone();

        List<CtTypeReference<?>> typeCasts = ctExpression.getTypeCasts();
        if (!typeCasts.isEmpty() && typeCasts.get(typeCasts.size()-1).equals(targetType)) {
            return result;
        }

        // only add a cast if it is necessary
        if (ctExpression.getType().equals(targetType)) {
            return result;
        }

        if (requiresCast(ctExpression, targetType)) {
            result.addTypeCast(targetType.clone());
        }

        return ctExpression.setType(targetType.clone());
    }

    public static boolean isEqualsMethod(CtMethod<?> method) {
        return SpoonUtil.isSignatureEqualTo(
            method.getReference(),
            boolean.class,
            "equals",
            java.lang.Object.class
        );
    }

    public static boolean isCompareToMethod(CtMethod<?> method) {
        return method.isPublic()
                && SpoonUtil.isSignatureEqualTo(
                    method.getReference(),
                    method.getFactory().Type().createReference(int.class),
                    "compareTo",
                    method.getDeclaringType().getReference()
                );
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

    public static boolean isEffectivelyFinal(StaticAnalysis staticAnalysis, CtVariableReference<?> ctVariableReference) {
        return isEffectivelyFinal(staticAnalysis.getModel(), ctVariableReference);
    }

    public static boolean isEffectivelyFinal(CtVariableReference<?> ctVariableReference) {
        return isEffectivelyFinal(ctVariableReference.getFactory().getModel(), ctVariableReference);
    }

    public static boolean isEffectivelyFinal(CtModel ctModel, CtVariableReference<?> ctVariableReference) {
        if (ctVariableReference instanceof CtFieldReference<?> field) {
            if (field.getDeclaringType().isArray() || field.getDeclaringType().isPrimitive()) {
                // calling getModifiers() on (new int[1]).length throws a Spoon exception: "The field int#length not found"
                // Probably a bug in Spoon
                return false;
            }
        }

        return ctVariableReference.getModifiers().contains(ModifierKind.FINAL) || ctModel
                .filterChildren(e -> e instanceof CtVariableWrite<?> write &&
                        write.getVariable().equals(ctVariableReference))
                .first() == null;
    }

    public static Optional<CtExpression<?>> getEffectivelyFinalExpression(
        CtVariableReference<?> ctVariableReference
    ) {
        return getEffectivelyFinalExpression(ctVariableReference.getFactory().getModel(), ctVariableReference);
    }

    public static Optional<CtExpression<?>> getEffectivelyFinalExpression(
            CtModel ctModel,
            CtVariableReference<?> ctVariableReference
    ) {
        if (!isEffectivelyFinal(ctModel, ctVariableReference)) {
            return Optional.empty();
        }

        if (ctVariableReference.getDeclaration() == null) {
            // this pointer
            return Optional.empty();
        }

        return Optional.ofNullable(ctVariableReference.getDeclaration().getDefaultExpression());
    }

    public static Optional<CtExpression<?>> getEffectivelyFinalExpression(
        StaticAnalysis staticAnalysis,
        CtVariableReference<?> ctVariableReference
    ) {
        if (!isEffectivelyFinal(staticAnalysis, ctVariableReference)) {
            return Optional.empty();
        }

        return Optional.ofNullable(ctVariableReference.getDeclaration().getDefaultExpression());
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
        return ctTypeReference.isSubtypeOf(ctTypeReference.getFactory().Type().createReference(expected));
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

    public static boolean isOverriddenMethod(CtMethod<?> ctMethod) {
        // if the method is defined for the first time, this should return an empty collection
        return !ctMethod.getTopDefinitions().isEmpty();
    }

    public static boolean isInOverriddenMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return isOverriddenMethod(ctMethod);
    }

    public static List<CtMethod<?>> getOverridingMethods(CtMethod<?> ctMethod) {
        return ctMethod.getFactory().getModel().getElements(new OverridingMethodFilter(ctMethod).includingSelf(false));
    }

    public static boolean isInMainMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return isMainMethod(ctMethod);
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
}
