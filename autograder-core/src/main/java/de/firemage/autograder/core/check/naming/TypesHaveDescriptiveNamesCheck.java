package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;

import java.util.List;

@ExecutableCheck(reportedProblems = {ProblemType.TYPE_HAS_DESCRIPTIVE_NAME })
public class TypesHaveDescriptiveNamesCheck extends IntegratedCheck {
    private static final List<String> BAD_PREFIXES_SUFFIXES =
        List.of("object", "class", "record", "interface", "enum");

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> ctType) {
                if (ctType.isImplicit() || !ctType.getPosition().isValidPosition()) return;

                boolean hasBadName = BAD_PREFIXES_SUFFIXES
                        .stream()
                        .anyMatch(name -> ctType.getSimpleName().toLowerCase().startsWith(name) || ctType.getSimpleName().toLowerCase().endsWith(name));

                if (hasBadName) {
                    addLocalProblem(
                        ctType,
                        new LocalizedMessage("type-has-descriptive-name-pre-suffix"),
                        ProblemType.TYPE_HAS_DESCRIPTIVE_NAME
                    );
                }

                if (ctType.isSubtypeOf(ctType.getFactory().Type().createReference(java.lang.Exception.class)) && !ctType.getSimpleName().endsWith("Exception")) {
                    addLocalProblem(
                        ctType,
                        new LocalizedMessage("type-has-descriptive-name-exception"),
                        ProblemType.TYPE_HAS_DESCRIPTIVE_NAME
                    );
                }
            }
        });
    }
}
