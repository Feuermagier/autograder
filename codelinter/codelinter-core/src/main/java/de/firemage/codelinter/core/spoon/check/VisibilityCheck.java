package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;

public class VisibilityCheck extends AbstractLoggingProcessor<CtField<?>> {
    private static final String DESCRIPTION = "Variable should be private";
    private static final String EXPLANATION = """
            Everybody using this class now depends on the existence of the field in its current form.
            To enforce loose coupling, set it to private.
            If other classes access this field, create a getter and, if needed, create a setter.""";


    public VisibilityCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtField<?> field) {
        if (!field.getVisibility().equals(ModifierKind.PRIVATE) && !(field.isStatic() && field.isFinal())) {
            this.addProblem(field, DESCRIPTION, ProblemCategory.OOP, EXPLANATION, ProblemPriority.FIX_RECOMMENDED);
        }
    }
}
