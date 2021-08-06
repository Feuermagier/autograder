package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
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
            addProblem(element, DESCRIPTION, ProblemCategory.OOP, EXPLANATION, ProblemPriority.FIX_RECOMMENDED);
        }
    }
}
