package de.firemage.codelinter.linter.spoon.check.reflect;

import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.check.AbstractLoggingProcessor;
import spoon.reflect.declaration.CtImport;

public class ReflectImportCheck extends AbstractLoggingProcessor<CtImport> {
    private static final String DESCRIPTION = "Imported java.lang.reflect";
    private static final String EXPLANATION = """
            Using Java reflection indicates bad design in the context of programming lectures.
            Boilerplate code is always better then possibly breaking OOP best practices.""";

    public ReflectImportCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtImport element) {
        System.out.println(element);
    }
}
