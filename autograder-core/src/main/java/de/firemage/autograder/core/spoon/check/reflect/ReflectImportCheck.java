package de.firemage.autograder.core.spoon.check.reflect;

import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.spoon.check.AbstractCompilationUnitCheck;
import spoon.reflect.declaration.CtCompilationUnit;

public class ReflectImportCheck extends AbstractCompilationUnitCheck {
    private static final String DESCRIPTION = "Imported java.lang.reflect";
    private static final String EXPLANATION = """
            Using Java reflection indicates bad design in the context of programming lectures.
            Boilerplate code is always better then possibly breaking OOP best practices.""";

    public ReflectImportCheck(Check check) {
        super(check);
    }

    @Override
    public void checkCompilationUnit(CtCompilationUnit compilationUnit) {
        compilationUnit.getImports().forEach(i -> {
            if (i.toString().contains("java.lang.reflect")) {
                addProblem(i, DESCRIPTION);
            }
        });
    }
}
