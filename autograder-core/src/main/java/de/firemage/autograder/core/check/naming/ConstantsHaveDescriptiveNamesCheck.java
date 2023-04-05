package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {ProblemType.MEANINGLESS_CONSTANT_NAME})
public class ConstantsHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final List<String> NUMBER_PRE_SUFFIXES =
        List.of("index", "number", "value", "argument", "element", "param", "parameter", "arg", "group");

    public ConstantsHaveDescriptiveNamesCheck() {
        super(new LocalizedMessage("constants-name-desc"));
    }

    private static boolean isNonDescriptiveIntegerName(String name, int value) {
        List<String> valueNameOptions = switch (value) {
            case 0 -> List.of("zero", "null", "zeroth", "first");
            case -1 -> List.of("minusone", "minus_one", "negative_one", "negativone", "neg_one", "negone");
            case 1 -> List.of("one", "second");
            case 2 -> List.of("two", "third");
            case 3 -> List.of("three", "fourth");
            default -> List.of();
        };
        return valueNameOptions.stream()
            .flatMap(o -> Stream.concat(Stream.of(o),
                NUMBER_PRE_SUFFIXES.stream().flatMap(s -> Stream.of(s + "_" + o, o + "_" + s))))
            .anyMatch(o -> name.toLowerCase().equals(o));
    }

    private static boolean isNonDescriptiveStringName(String name, String value) {
        if (value.length() > 4) {
            return false;
        }
        Stream<String> options = Stream.of("");
        if (value.isEmpty()) {
            options = Stream.of("empty", "blank");
        } else {
            for (char c : value.toCharArray()) {
                var charOptions = listCharOptions(c);
                if (charOptions == null) {
                    return false;
                }
                options = options.flatMap(
                    suffix -> charOptions.stream().flatMap(o -> Stream.of(suffix + o, suffix + "_" + o)));
            }
        }
        return options.anyMatch(option -> option.equals(name.toLowerCase()));
    }

    private static List<String> listCharOptions(char c) {
        return switch (c) {
            case ' ' -> List.of("space", "whitespace", "white_space");
            case ',' -> List.of("comma");
            case '.' -> List.of("point", "dot", "fullstop", "full_stop");
            case '-' -> List.of("minus", "hyphen", "dash", "line");
            case ':' -> List.of("colon");
            case ';' -> List.of("semi_colon", "semicolon");
            case '_' -> List.of("underscore", "dash", "line");
            default -> Character.isAlphabetic(c) ? List.of(Character.toLowerCase(c) + "") : null;
        };
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (!field.isFinal() || field.getDefaultExpression() == null) {
                    return;
                }

                if (field.getDefaultExpression() instanceof CtLiteral<?> literal) {
                    if (literal.getValue() instanceof Integer value &&
                        isNonDescriptiveIntegerName(field.getSimpleName(), value)) {
                        addLocalProblem(field,
                            new LocalizedMessage("constants-name-exp-number",
                                Map.of(
                                    "name", field.getSimpleName(),
                                    "value", value
                                )), ProblemType.MEANINGLESS_CONSTANT_NAME);
                    } else if (literal.getValue() instanceof String value &&
                        isNonDescriptiveStringName(field.getSimpleName(), value)) {
                        addLocalProblem(field,
                            new LocalizedMessage("constants-name-exp-string",
                                Map.of(
                                    "name", field.getSimpleName(),
                                    "value", value
                                )), ProblemType.MEANINGLESS_CONSTANT_NAME);
                    }
                }
            }
        });
    }
}
