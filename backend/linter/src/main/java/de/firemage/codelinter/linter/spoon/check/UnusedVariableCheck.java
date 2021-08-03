package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.SpoonInCodeProblem;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class UnusedVariableCheck implements Check {
    public static final String DESCRIPTION = "Unused variable '%s'";
    public static final String EXPLANATION = """
            You don't use the variable, so why declare it?
            """;

    private final ProblemLogger logger;

    public UnusedVariableCheck(ProblemLogger logger) {
        this.logger = logger;
    }

    @Override
    public void check(CtModel model, Factory factory) {
        Set<CtVariable<?>> usedVariables = new HashSet<>();

        Query.getElements(factory, new TypeFilter<>(CtVariableRead.class)).stream()
                .map(CtVariableAccess::getVariable)
                .filter(Objects::nonNull)
                .map(CtVariableReference::getDeclaration)
                .forEach(usedVariables::add);

        Query.getElements(factory, new TypeFilter<>(CtVariable.class)).stream()
                .filter(Predicate.not(usedVariables::contains))
                .filter(Predicate.not(v -> v instanceof CtParameter<?>))
                .filter(Predicate.not(v -> v instanceof CtEnumValue<?>))
                .forEach(v -> logger.addProblem(new SpoonInCodeProblem(v, String.format(DESCRIPTION, v.getSimpleName()), ProblemCategory.OTHER, EXPLANATION, ProblemPriority.FIX_RECOMMENDED)));
    }
}
