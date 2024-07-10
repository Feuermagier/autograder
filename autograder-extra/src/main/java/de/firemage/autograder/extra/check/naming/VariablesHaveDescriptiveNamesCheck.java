package de.firemage.autograder.extra.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.extra.integrated.IdentifierNameUtils;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.MethodUtil;
import org.apache.commons.lang3.StringUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExecutableCheck(reportedProblems = {
    ProblemType.SINGLE_LETTER_LOCAL_NAME,
    ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE,
    ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME,
    ProblemType.SIMILAR_IDENTIFIER,
    ProblemType.IDENTIFIER_REDUNDANT_NUMBER_SUFFIX
})
public class VariablesHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final Set<String> ALLOWED_IDENTIFIER = Set.of(
        "x1", "x2", "x3",
        "y1", "y2", "y3",
        "z1", "z2", "z3",
        "ui" // allowed abbreviation
    );

    private static final List<String> TYPE_NAMES = List.of(
        "string", "list", "array", "map", "set", "int", "long", "float"
    );

    private static final Set<String> ALLOWED_NAMES_WITH_TYPES = Set.of(
        "subList",
        "subString"
    );

    private static final Set<String> KNOWN_ABBREVIATIONS = Set.of(
        "sec", "min"
    );

    private static final Set<String> ALLOWED_SIMILAR_IDENTIFIER = Set.of(
        "july", "june"
    );

    private static final Set<String> ALLOWED_SIMILAR_IDENTIFIER_CONTAINS = Set.of(
        "max", "min", "maximum", "minimum"
    );

    private final Set<String> similarIdentifier = new HashSet<>();

    private static Set<String> typeNameAlternatives(String typeName) {
        Set<String> result = new HashSet<>(Set.of(typeName));

        if (!typeName.endsWith("s")) {
            // string -> strings, int -> ints, ...
            result.add(typeName + "s");
        }

        if (typeName.equals("int")) {
            result.addAll(typeNameAlternatives("integer"));
        }

        return result;
    }

    private static boolean hasTypeInName(CtNamedElement ctVariable) {
        if (ALLOWED_NAMES_WITH_TYPES.contains(ctVariable.getSimpleName())) {
            return false;
        }

        List<String> referencedTypeNames = ctVariable.getReferencedTypes()
            .stream()
            .map(CtTypeReference::getSimpleName)
            .map(String::toLowerCase)
            .filter(TYPE_NAMES::contains)
            .flatMap(name -> typeNameAlternatives(name).stream())
            .toList();

        Set<String> words = IdentifierNameUtils.split(ctVariable.getSimpleName()).collect(Collectors.toSet());
        if (words.size() == 1) {
            // if the variable name is only one word, any violation would be the type name itself
            // like `int integer` or `String string`, which is explicitly allowed
            return false;
        }

        return referencedTypeNames.stream().anyMatch(words::contains);
    }

    private static boolean isLambdaParameter(CtVariable<?> variable) {
        return variable instanceof CtParameter<?> && variable.getParent() instanceof CtLambda<?>;
    }

    private static boolean isCoordinate(CtVariable<?> variable) {
        return (variable.getSimpleName().equals("x") || variable.getSimpleName().equals("y"));
    }

    private static boolean isAllowedLoopCounter(CtVariable<?> variable) {
        var role = variable.getRoleInParent();
        if (role != CtRole.FOR_INIT && role != CtRole.FOREACH_VARIABLE) {
            return false;
        }

        // for (char c : string.toCharArray())
        if (variable.getType().getQualifiedName().equals("char") && variable.getSimpleName().equals("c")) {
            return true;
        }

        // for (int i = 0; i < 10; i++)
        return SpoonUtil.isPrimitiveNumeric(variable.getType());
    }

    private static boolean isAbbreviation(CtVariable<?> variable) {
        if (KNOWN_ABBREVIATIONS.contains(variable.getSimpleName())) {
            return true;
        }

        if (variable.getType().isPrimitive()) {
            return false;
        }

        String name = variable.getSimpleName();
        String type = variable.getType().getSimpleName();
        String[] parts = StringUtils.splitByCharacterTypeCamelCase(type);

        if (parts[0].length() >= 4 && name.length() <= 3 && parts[0].toLowerCase().indexOf(name) == 0) {
            // Scanner -> sc
            return true;
        } else if (parts.length == name.length()) {
            // MyCoolClass -> mcc
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].toLowerCase().charAt(0) != name.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Calculates a value to determine how similar two variable names are.
     *
     * @param variable the first variable
     * @param other    the second variable
     * @return a value of 0 means they are equal, a value of 1 means they differ at one character, a value of 2 means they differ by two characters, ...
     */
    private static int similarity(CtNamedElement variable, CtNamedElement other) {
        String name = variable.getSimpleName();
        String otherName = other.getSimpleName();

        int similarity = 0;

        if (name.length() != otherName.length()) {
            similarity += Math.abs(name.length() - otherName.length());
        }

        for (int i = 0; i < Math.min(name.length(), otherName.length()); i++) {
            if (name.charAt(i) != otherName.charAt(i)) {
                similarity += 1;
            }
        }

        return similarity;
    }

    private static boolean areSimilar(CtNamedElement variable, CtNamedElement other) {
        if (ALLOWED_SIMILAR_IDENTIFIER.contains(variable.getSimpleName().toLowerCase())
            || ALLOWED_SIMILAR_IDENTIFIER.contains(other.getSimpleName().toLowerCase())) {
            return false;
        }

        if (ALLOWED_SIMILAR_IDENTIFIER_CONTAINS.stream().anyMatch(variable.getSimpleName().toLowerCase()::contains)
            || ALLOWED_SIMILAR_IDENTIFIER_CONTAINS.stream().anyMatch(other.getSimpleName().toLowerCase()::contains)) {
            return false;
        }

        return similarity(variable, other) <= 1;
    }

    private static <I, O> Stream<O> filterByType(Stream<I> stream, Class<? extends O> type) {
        return stream.filter(type::isInstance).map(type::cast);
    }

    private static List<CtNamedElement> getSiblings(CtNamedElement ctNamedElement) {
        List<CtNamedElement> result = new ArrayList<>();

        if (ctNamedElement instanceof CtParameter<?> ctParameter
            && ctNamedElement.getParent() instanceof CtExecutable<?> ctExecutable
            && ctExecutable.getParameters().contains(ctParameter)) {
            result.addAll(ctExecutable.getParameters());
            result.remove(ctParameter);

            return result;
        }

        if (ctNamedElement instanceof CtField<?> ctField) {
            CtType<?> ctType = ctField.getDeclaringType();
            if (ctType == null) return result;

            result.addAll(ctType.getFields());
            result.remove(ctField);

            return result;
        }

        if (ctNamedElement instanceof CtLocalVariable<?> ctLocalVariable && ctLocalVariable.getParent() instanceof CtStatementList ctStatementList) {
            // add all declared variables from the same scope
            result.addAll(filterByType(ctStatementList.getStatements().stream(), CtVariable.class).toList());
            result.remove(ctLocalVariable);

            return result;
        }

        return result;
    }

    private static String removeNumberSuffix(String name) {
        return name.replaceAll("\\d*$", "");
    }

    private static boolean hasRedundantNumberSuffix(CtNamedElement ctVariable) {
        String name = ctVariable.getSimpleName();

        // this tries to detect a variable like result1 that could be renamed to result

        String nameWithoutNumbers = removeNumberSuffix(name);
        if (nameWithoutNumbers.equals(name) || nameWithoutNumbers.isEmpty()) {
            return false;
        }

        // check that the name without numbers is not used by another variable
        return getSiblings(ctVariable)
            .stream()
            .map(CtNamedElement::getSimpleName)
            // to prevent false-positives with "result1" and "result2"
            .map(VariablesHaveDescriptiveNamesCheck::removeNumberSuffix)
            .noneMatch(nameWithoutNumbers::equals);
    }

    private void reportProblem(String key, CtNamedElement ctVariable, ProblemType problemType) {
        this.addLocalProblem(
            ctVariable,
            new LocalizedMessage(key, Map.of("name", ctVariable.getSimpleName())),
            problemType
        );
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtVariable<?>>() {
            @Override
            public void process(CtVariable<?> ctVariable) {
                if (ctVariable.isImplicit() || !ctVariable.getPosition().isValidPosition()) {
                    return;
                }

                if (ctVariable instanceof CtCatchVariable || isLambdaParameter(ctVariable)) {
                    // Catch vars and lambda vars have less strict rules, e.g. it is ok to write "Exception e" or "NullPointerException npe"
                    return;
                }

                if (MethodUtil.isInOverridingMethod(ctVariable)) {
                    // The parameter of the equals and compareTo methods may be named "o", "obj", ...
                    //
                    // skip all overridden methods for consistency
                    return;
                }

                if (ALLOWED_IDENTIFIER.contains(ctVariable.getSimpleName())) {
                    return;
                }

                if (ctVariable.getSimpleName().length() == 1
                    && !isAllowedLoopCounter(ctVariable)
                    && !isCoordinate(ctVariable)) {
                    reportProblem("variable-name-single-letter", ctVariable, ProblemType.SINGLE_LETTER_LOCAL_NAME);
                    return;
                }

                if (isAbbreviation(ctVariable)) {
                    reportProblem("variable-is-abbreviation", ctVariable, ProblemType.IDENTIFIER_IS_ABBREVIATED_TYPE);
                    return;
                }

                if (hasTypeInName(ctVariable)) {
                    reportProblem(
                        "variable-name-type-in-name",
                        ctVariable,
                        ProblemType.IDENTIFIER_CONTAINS_TYPE_NAME
                    );
                    return;
                }

                if (hasRedundantNumberSuffix(ctVariable)) {
                    reportProblem(
                        "variable-redundant-number-suffix",
                        ctVariable,
                        ProblemType.IDENTIFIER_REDUNDANT_NUMBER_SUFFIX
                    );
                    return;
                }

                if (!similarIdentifier.contains(ctVariable.getSimpleName())) {
                    for (CtNamedElement sibling : getSiblings(ctVariable)) {
                        if (areSimilar(ctVariable, sibling) && !similarIdentifier.contains(sibling.getSimpleName())) {
                            addLocalProblem(
                                sibling,
                                new LocalizedMessage(
                                    "similar-identifier",
                                    Map.of(
                                        "left",
                                        ctVariable.getSimpleName(),
                                        "right",
                                        sibling.getSimpleName()
                                    )
                                ),
                                ProblemType.SIMILAR_IDENTIFIER
                            );

                            similarIdentifier.add(ctVariable.getSimpleName());
                            similarIdentifier.add(sibling.getSimpleName());
                        }
                    }
                    return;
                }
            }
        });
    }
}
