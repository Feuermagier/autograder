package de.firemage.autograder.core.check.general;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.compiler.CompilationDiagnostic;
import de.firemage.autograder.core.compiler.CompilationResult;
import de.firemage.autograder.core.dynamic.DynamicAnalysis;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;

import java.util.List;

@ExecutableCheck(reportedProblems = { ProblemType.UNCHECKED_TYPE_CAST })
public class UncheckedTypeCast extends IntegratedCheck {
    private static final List<String> WARNING_CODES = List.of(
        // the code for an unchecked cast
        "compiler.warn.prob.found.req"
    );

    @Override
    protected void check(StaticAnalysis staticAnalysis, DynamicAnalysis dynamicAnalysis) {
        CompilationResult result = staticAnalysis.getCompilationResult();
        List<CompilationDiagnostic> diagnostics = result.diagnostics()
              .stream()
              .filter(diagnostic -> WARNING_CODES.contains(diagnostic.code()))
              .toList();

        for (CompilationDiagnostic diagnostic : diagnostics) {
            addLocalProblem(
                diagnostic.codePosition(),
                new LocalizedMessage("unchecked-type-cast"),
                ProblemType.UNCHECKED_TYPE_CAST
            );
        }
    }
}
