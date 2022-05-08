package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.declaration.CtVariable;

public class ObjectTypeCheck extends AbstractLoggingProcessor<CtVariable<?>> {
    public static final String DESCRIPTION = "Used 'Object'";
    public static final String EXPLANATION = """
            Outside of 'equals' you never need to use java.lang.Object directly.
            If you want to allow multiple incompatible types, consider Generics.
            """;

    public ObjectTypeCheck(Check check) {
        super(check);
    }

    @Override
    public void process(CtVariable<?> element) {
        if (element.getType().getQualifiedName().equals("java.lang.Object")
                && !CheckUtil.isInEquals(element)) {
            addProblem(element, DESCRIPTION);
        }
    }
}
