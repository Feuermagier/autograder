package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.integrated.effects.AssignmentStatement;
import de.firemage.autograder.core.integrated.effects.Effect;
import de.firemage.autograder.core.integrated.effects.TerminalEffect;
import de.firemage.autograder.core.integrated.effects.TerminalStatement;
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
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.support.reflect.code.CtLiteralImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return expression instanceof CtLiteral<?> literal && literal.getValue() == null;
    }

    public static boolean isIntegerLiteral(CtExpression<?> expression, int value) {
        return expression instanceof CtLiteral<?> literal && literal.getValue().equals(value);
    }

    public static boolean isStringLiteral(CtExpression<?> expression, String value) {
        return expression instanceof CtLiteral<?> literal && literal.getValue() != null &&
               literal.getValue().equals(value);
    }

    public static Optional<Boolean> tryGetBooleanLiteral(CtExpression<?> expression) {
        if (expression instanceof CtLiteral<?> literal
            && literal.getValue() != null
            && (isTypeEqualTo(literal.getType(), boolean.class) ||
                isTypeEqualTo(literal.getType(), java.lang.Boolean.class))) {

            return Optional.of((Boolean) literal.getValue());
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

    public static List<CtStatement> getEffectiveStatements(CtStatement ctStatement) {
        if (ctStatement instanceof CtStatementList ctStatementList) {
            return getEffectiveStatements(ctStatementList.getStatements());
        }

        return getEffectiveStatements(List.of(ctStatement));
    }

    public static CtExpression<?> resolveCtExpression(CtExpression<?> ctExpression) {
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
                return ctLiteral;
            }
        }

        // evaluate concatenations of (potentially) literals:
        if (ctExpression instanceof CtBinaryOperator<?> ctBinaryOperator) {
            CtExpression<?> left = resolveCtExpression(ctBinaryOperator.getLeftHandOperand());
            CtExpression<?> right = resolveCtExpression(ctBinaryOperator.getRightHandOperand());

            // check if both resolve to literals
            if (left instanceof CtLiteral<?> leftLiteral
                    && right instanceof CtLiteral<?> rightLiteral
                    && ctBinaryOperator.getKind() == BinaryOperatorKind.PLUS) {
                // check if one of them is a string, if so then we can concatenate them
                if (leftLiteral.getValue() instanceof String string) {
                    return makeLiteral(string + rightLiteral.getValue());
                } else if (rightLiteral.getValue() instanceof String string) {
                    return makeLiteral(leftLiteral.getValue() + string);
                }
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

    public static boolean isEqualsMethod(CtMethod<?> method) {
        return method.getSimpleName().equals("equals")
               && method.isPublic()
               && method.getType().getQualifiedName().equals("boolean")
               && method.getParameters().size() == 1
               && method.getParameters().get(0).getType().getQualifiedName().equals("java.lang.Object");
    }

    public static boolean isCompareToMethod(CtMethod<?> method) {
        return method.getSimpleName().equals("compareTo")
               && method.isPublic()
               && method.getType().getQualifiedName().equals("boolean")
               && method.getParameters().size() == 1
               && method.getParameters().get(0).getType().getQualifiedName()
                        .equals(method.getDeclaringType().getQualifiedName());
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

    public static boolean isTypeEqualTo(CtTypeReference<?> ctType, Class<?> expectedClass) {
        return ctType.getFactory().Type().createReference(expectedClass).equals(ctType);
    }

    public static boolean isMainMethod(CtMethod<?> method) {
        return method.getSimpleName().equals("main")
            && method.getType().getQualifiedName().equals("void")
            && method.isStatic()
            && method.isPublic()
            && method.getParameters().size() == 1
            && method.getParameters().get(0).getType().getQualifiedName().equals("java.lang.String[]");
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
        return String.format("%s:L%d", sourcePosition.getFile().getName(), sourcePosition.getLine());
    }
}
