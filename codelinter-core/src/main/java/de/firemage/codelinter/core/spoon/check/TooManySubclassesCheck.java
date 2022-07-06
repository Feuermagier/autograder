package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.check.Check;
import spoon.reflect.declaration.CtClass;

public class TooManySubclassesCheck extends AbstractLoggingProcessor<CtClass<?>> {
    //TODO This number is completely arbitrary
    private static final double ALLOWED_INHERITANCE_RATIO = 0.6;

    public static final String DESCRIPTION = "Many classes inherit other classes";
    public static final String EXPLANATION = """
            OOP is great, but not all classes have to inherit other classes.
            The 'composition over inheritance' principal applies:
            If there is no semantic relationship between two classes, common functionality
            should be factored out into helper classes.""";

    private int totalTypeCount = 0;
    private int typeWithParentCount = 0;

    public TooManySubclassesCheck(Check check) {
        super(check);
    }

    @Override
    public void process(CtClass<?> element) {
        totalTypeCount++;
        if (element.getSuperclass() != null && !element.getSuperclass().getQualifiedName().equals("java.lang.Object")) {
            typeWithParentCount++;
        }
    }

    @Override
    protected void processingFinished() {
        if (typeWithParentCount / ((double) totalTypeCount) > ALLOWED_INHERITANCE_RATIO) {
            addProblem(DESCRIPTION);
        }
    }
}
