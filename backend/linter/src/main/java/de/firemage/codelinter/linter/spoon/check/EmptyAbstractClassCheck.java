package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class EmptyAbstractClassCheck extends AbstractLoggingProcessor<CtClass<?>> {
    public static final String DESCRIPTION = "Class with only abstract methods";
    public static final String EXPLANATION = """
            The class could be converted to an interface because it has only abstract methods.
            Ignore this if problem if you used an abstract class for a specific reason.""";

    public EmptyAbstractClassCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtClass<?> element) {
        if (element.isAbstract() && element.getMethods().stream().allMatch(CtMethod::isAbstract)) {
            addProblem(new InCodeProblem(element, DESCRIPTION, ProblemCategory.OOP, EXPLANATION));
        }
    }
}
