package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.stream.Stream;

public class ConstantsHaveDescriptiveNamesCheck extends IntegratedCheck {

    public ConstantsHaveDescriptiveNamesCheck() {
        super("Constants should have descriptive names - e.g. AUTHOR_INDEX instead of FIRST_INDEX");
    }

    private static boolean isNonDescriptiveIntegerName(String name, int value) {
        List<String> valueNameOptions = switch (value) {
            case 0 -> List.of("zero", "null", "zeroth", "first");
            case -1 -> List.of("minusone", "minus_one", "negative_one", "negativone");
            case 1 -> List.of("one", "second");
            case 2 -> List.of("two", "third");
            case 3 -> List.of("three", "fourth");
            default -> List.of();
        };
        return valueNameOptions.stream()
            .flatMap(o -> Stream.of(o, o + "_index", "index_" + o, o + "_element", "element_" + o, o + "_value",
                "value_" + o))
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
                options = options.flatMap(suffix -> charOptions.stream().flatMap(o -> Stream.of(suffix + o, suffix + "_" + o)));
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
                            String.format("The name '%s' is non-descriptive for the value %d", field.getSimpleName(),
                                value));
                    } else if (literal.getValue() instanceof String value &&
                        isNonDescriptiveStringName(field.getSimpleName(), value)) {
                        addLocalProblem(field,
                            String.format("The name '%s' is non-descriptive for the value '%s'", field.getSimpleName(),
                                value));
                    }
                }
            }
        });
    }
}
