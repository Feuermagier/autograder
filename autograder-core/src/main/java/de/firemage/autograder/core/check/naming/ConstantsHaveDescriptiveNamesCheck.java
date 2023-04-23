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
import spoon.reflect.declaration.CtVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {ProblemType.MEANINGLESS_CONSTANT_NAME})
public class ConstantsHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final List<String> NUMBER_PRE_SUFFIXES =
            List.of("index", "number", "value", "argument", "element", "param", "parameter", "arg", "group");

    private static final List<String> NON_DESCRIPTIVE_NAMES = List.of("error", "pattern", "regex", "symbol");
    private static final Map<String, List<String>> SPECIAL_VALUE_MAPPING = Map.ofEntries(
            Map.entry("->", List.of("arrow")),
            Map.entry("-->", List.of("arrow"))
    );

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
        if (NON_DESCRIPTIVE_NAMES.contains(name.toLowerCase())) {
            return true;
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
            case '/' -> List.of("slash", "backslash");
            case '\\' -> List.of("slash", "backslash");
            default -> Character.isAlphabetic(c) ? List.of(Character.toLowerCase(c) + "") : null;
        };
    }

    private static boolean containsValueInName(String name, CtLiteral<?> value) {
        String lowerCaseName = name.toLowerCase();

        String valueString = "null";
        if (value.getValue() != null) {
            valueString = value.getValue().toString().toLowerCase();
        }

        // convert special characters like : to their names (colon)
        if (valueString.length() == 1 && !Character.isAlphabetic(valueString.charAt(0))) {
            List<String> charOptions = listCharOptions(valueString.charAt(0));
            if (charOptions != null) {
                return charOptions.stream().anyMatch(lowerCaseName::contains);
            }
        }

        for (var entry : SPECIAL_VALUE_MAPPING.entrySet()) {
            if (valueString.contains(entry.getKey())) {
                return entry.getValue().stream().anyMatch(lowerCaseName::contains);
            }
        }

        // to detect private static String MY_CONSTANT = "my-constant"
        valueString = valueString.replace('-', '_');

        return lowerCaseName.contains(valueString);
    }

    private void reportProblem(String key, CtVariable<?> ctVariable) {
        this.addLocalProblem(
            ctVariable,
            new LocalizedMessage(
                key,
                Map.of(
                    "name", ctVariable.getSimpleName(),
                    "value", ctVariable.getDefaultExpression().prettyprint()
                )
            ),
            ProblemType.MEANINGLESS_CONSTANT_NAME
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtField<?>>() {
            @Override
            public void process(CtField<?> field) {
                if (!field.isFinal()
                    || field.getDefaultExpression() == null
                    || !(field.getDefaultExpression() instanceof CtLiteral<?> literal)) {
                    return;
                }

                String fieldName = field.getSimpleName();

                if (fieldName.length() == 1
                    || literal.getValue() instanceof Integer v1 && isNonDescriptiveIntegerName(fieldName, v1)
                    || literal.getValue() instanceof String v2 && isNonDescriptiveStringName(fieldName, v2)) {
                    reportProblem("constants-name-exp", field);
                } else if (containsValueInName(fieldName, literal)) {
                    reportProblem("constants-name-exp-value", field);
                }
            }
        });
    }
}
