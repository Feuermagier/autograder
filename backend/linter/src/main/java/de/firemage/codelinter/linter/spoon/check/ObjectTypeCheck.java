package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.ProblemCategory;
import de.firemage.codelinter.linter.ProblemPriority;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.SpoonInCodeProblem;
import spoon.reflect.declaration.CtVariable;

public class ObjectTypeCheck extends AbstractLoggingProcessor<CtVariable<?>> {
    public static final String DESCRIPTION = "Used 'Object'";
    public static final String EXPLANATION = """
            Outside of 'equals' you never need to use java.lang.Object directly.
            If you want to allow multiple incompatible types, consider Generics.
            """;

    public ObjectTypeCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtVariable<?> element) {
        if (element.getType().getQualifiedName().equals("java.lang.Object")
                && !CheckUtil.isInEquals(element)) {
            addProblem(new SpoonInCodeProblem(element, DESCRIPTION, ProblemCategory.OOP, EXPLANATION, ProblemPriority.SEVERE));
        }
    }
}
