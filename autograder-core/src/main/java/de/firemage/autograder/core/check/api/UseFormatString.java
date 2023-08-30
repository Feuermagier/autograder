package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

@ExecutableCheck(reportedProblems = { ProblemType.USE_FORMAT_STRING })
public class UseFormatString extends IntegratedCheck {
    private static final int MIN_NUMBER_CONCATENATIONS = 3;

    private List<CtExpression<?>> getFormatArgs(CtBinaryOperator<?> ctBinaryOperator) {
        List<CtExpression<?>> result = new ArrayList<>();

        CtExpression<?> left = ctBinaryOperator.getLeftHandOperand();
        CtExpression<?> right = ctBinaryOperator.getRightHandOperand();

        result.add(right);

        while (left instanceof CtBinaryOperator<?> lhs) {
            result.add(lhs.getRightHandOperand());
            left = lhs.getLeftHandOperand();
        }

        result.add(left);

        Collections.reverse(result);

        return result;
    }

    private String getFormatPlaceholder(CtTypeReference<?> ctTypeReference) {
        // See https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Formatter.html
        if (ctTypeReference.isPrimitive()) {
            return switch (ctTypeReference.getSimpleName()) {
                case "boolean" -> "%b";
                case "char" -> "%c";
                case "byte", "short", "int", "long" -> "%d";
                case "float", "double" -> "%f";
                default -> "%s";
            };
        }

        return "%s";
    }

    private String buildFormattedString(Iterable<? extends CtExpression<?>> ctExpressions) {
        StringBuilder formatString = new StringBuilder();
        Collection<String> args = new ArrayList<>();

        // true if the formatString has a %n placeholder
        boolean hasInlineNewline = false;
        for (CtExpression<?> ctExpression : ctExpressions) {
            if (ctExpression instanceof CtLiteral<?> ctLiteral) {
                CtTypeInformation ctTypeInformation = ctLiteral.getType();
                if (ctLiteral.getValue() instanceof String value) {
                    // replace a system-dependant newline with %n
                    if (value.equals("\n")) {
                        hasInlineNewline = true;
                        formatString.append("%n");
                    } else {
                        formatString.append(value);
                    }
                } else if (ctTypeInformation.isPrimitive() && !ctTypeInformation.isArray()) {
                    // inline literals:
                    formatString.append(ctLiteral.getValue());
                }

                continue;
            }

            formatString.append(this.getFormatPlaceholder(ctExpression.getType()));
            args.add(ctExpression.prettyprint());
        }

        if (args.isEmpty() && !hasInlineNewline) {
            return "\"%s\"".formatted(formatString.toString());
        }

        if (hasInlineNewline && args.isEmpty()) {
            return null;
        }

        return "\"%s\".formatted(%s)".formatted(formatString.toString(), String.join(", ", args));
    }

    private CtExpression<?> resolveExpression(CtExpression<?> ctExpression) {
        // convert System.lineSeparator() to "\n" which will later be converted to %n
        if (ctExpression instanceof CtInvocation<?> ctInvocation
            && ctInvocation.getTarget() instanceof CtTypeAccess<?> ctTypeAccess
            // ensure the method is called on java.lang.System
            && ctInvocation.getFactory().Type().createReference(java.lang.System.class)
                .equals(ctTypeAccess.getAccessedType())
            && ctInvocation.getExecutable().getSimpleName().equals("lineSeparator")) {
            return SpoonUtil.makeLiteral("\n");
        }

        if (ctExpression instanceof CtLiteral<?> ctLiteral
            && SpoonUtil.areLiteralsEqual(ctLiteral, SpoonUtil.makeLiteral('\n'))) {
            return SpoonUtil.makeLiteral("\n");
        }

        return ctExpression;
    }

    private void checkArgs(CtElement ctElement, Iterable<? extends CtExpression<?>> formatArgs, UnaryOperator<String> suggestion) {
        Collection<CtExpression<?>> args = new ArrayList<>();
        for (var expression : formatArgs) {
            args.add(this.resolveExpression(expression));
        }

        // skip concatenations with less than 3 arguments
        if (args.size() < MIN_NUMBER_CONCATENATIONS) {
            return;
        }

        String formattedString = this.buildFormattedString(args);
        if (formattedString == null) return;

        this.addLocalProblem(
            ctElement,
            new LocalizedMessage("use-format-string", Map.of("formatted", suggestion.apply(formattedString))),
            ProblemType.USE_FORMAT_STRING
        );
    }

    private void checkCtBinaryOperator(CtBinaryOperator<?> ctBinaryOperator) {
        // Do not visit nested binary operators
        //
        // For example this expression:
        // "a" + "b" + "c"
        //
        // will be represented in spoon as:
        // (("a" + "b") + "c")
        //
        // so we only want to visit the outermost binary operator
        if (ctBinaryOperator.getParent(CtBinaryOperator.class) != null) {
            return;
        }

        if (ctBinaryOperator.getKind() != BinaryOperatorKind.PLUS) {
            return;
        }

        // only visit binary operators that evaluate to a String
        // (should be guaranteed by the visitor) -> seems to not be guaranteed; replacing the throw by a return for now
        if (!SpoonUtil.isString(ctBinaryOperator.getType())) {
            return;
        }

        this.checkArgs(ctBinaryOperator, this.getFormatArgs(ctBinaryOperator), suggestion -> suggestion);
    }

    private void checkCtInvocation(CtInvocation<?> ctInvocation) {
        // sb.append("a").append("b").append("c") instead of sb.append("abc")
        // same for sb.append("a").append(someVar) instead of sb.append("a%s", someVar)
        CtTypeReference<?> stringBuilderType = ctInvocation.getFactory().Type().createReference(java.lang.StringBuilder.class);
        if (!ctInvocation.getType().equals(stringBuilderType)) {
            return;
        }

        if (!ctInvocation.getExecutable().getSimpleName().equals("append")) {
            return;
        }

        // only visit the outermost invocations
        if (ctInvocation.getParent(CtInvocation.class) != null) return;

        List<CtExpression<?>> formatArgs = new ArrayList<>();
        CtExpression<?> invocationExpression = ctInvocation.getTarget();
        CtInvocation<?> currentInvocation = ctInvocation;
        // traverse the chain of append calls
        while (currentInvocation != null) {
            // if one of the calls is not an append call, early exit (e.g. sb.append("a").toString())
            if (!currentInvocation.getExecutable().getSimpleName().equals("append")) {
                return;
            }

            List<CtExpression<?>> arguments = currentInvocation.getArguments();
            // only one argument should be passed to append
            if (arguments.size() != 1) {
                return;
            }

            formatArgs.addAll(arguments);

            if (currentInvocation.getTarget() instanceof CtInvocation<?> ctInvocationTarget) {
                currentInvocation = ctInvocationTarget;
            // the last part of the chain should be an expression of type StringBuilder
            } else if (!stringBuilderType.equals(currentInvocation.getTarget().getType())) {
                return;
            } else {
                invocationExpression = currentInvocation.getTarget();
                currentInvocation = null;
            }
        }

        Collections.reverse(formatArgs);

        String target = invocationExpression.prettyprint();
        this.checkArgs(ctInvocation, formatArgs, suggestion -> "%s.append(%s)".formatted(target, suggestion));
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtExpression<String>>() {
            @Override
            public void process(CtExpression<String> ctExpression) {
                if (ctExpression instanceof CtBinaryOperator<?> ctBinaryOperator) {
                    checkCtBinaryOperator(ctBinaryOperator);
                } else if (ctExpression instanceof CtInvocation<?> ctInvocation) {
                    checkCtInvocation(ctInvocation);
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(5);
    }
}
