package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import de.firemage.autograder.core.integrated.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExecutableCheck(reportedProblems = { ProblemType.MAGIC_STRING })

public class MagicString extends IntegratedCheck {
    private static final Filter<CtLiteral<String>> IS_MAGIC_STRING = ctLiteral -> {
        // ignore empty values
        if (ctLiteral.getValue().isEmpty()) {
            return false;
        }

        // ignore strings that are in constants:
        CtField<?> parent = ctLiteral.getParent(CtField.class);
        if (parent != null && parent.isStatic() && parent.isFinal()) {
            return false;
        }

        return true;
    };

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {

            @Override
            @SuppressWarnings("unchecked")
            public void process(CtType<?> ctType) {
                if (ctType.isImplicit() || !ctType.getPosition().isValidPosition()) {
                    return;
                }

                List<CtLiteral<String>> magicStrings = ctType.getElements(new CompositeFilter<>(
                    FilteringOperator.INTERSECTION,
                    new TypeFilter<>(CtLiteral.class),
                    element -> element.getType() != null && TypeUtil.isTypeEqualTo(element.getType(), String.class),
                    IS_MAGIC_STRING
                ));

                Collection<String> reportedStrings = new HashSet<>();
                for (CtLiteral<String> magicString : magicStrings) {
                    if (!reportedStrings.add(magicString.getValue())) {
                        continue;
                    }

                    addLocalProblem(
                        magicString,
                        new LocalizedMessage(
                            "magic-string",
                            Map.of(
                                "value", magicString.getValue()
                            )
                        ),
                        ProblemType.MAGIC_STRING
                    );
                }
            }
        });
    }

    @Override
    public Optional<Integer> maximumProblems() {
        return Optional.of(1);
    }
}
