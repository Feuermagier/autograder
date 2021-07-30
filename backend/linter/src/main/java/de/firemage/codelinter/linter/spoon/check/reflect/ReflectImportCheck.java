package de.firemage.codelinter.linter.spoon.check.reflect;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.check.AbstractCompilationUnitCheck;
import spoon.reflect.declaration.CtCompilationUnit;

public class ReflectImportCheck extends AbstractCompilationUnitCheck {
    private static final String DESCRIPTION = "Imported java.lang.reflect";
    private static final String EXPLANATION = """
            Using Java reflection indicates bad design in the context of programming lectures.
            Boilerplate code is always better then possibly breaking OOP best practices.""";

    public ReflectImportCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void checkCompilationUnit(CtCompilationUnit compilationUnit) {
        compilationUnit.getImports().forEach(i -> {
            if (i.toString().contains("java.lang.reflect")) {
                addProblem(new SpoonInCodeProblem(i, DESCRIPTION, ProblemCategory.JAVA_FEATURE, EXPLANATION));
            }
        });
    }
}
