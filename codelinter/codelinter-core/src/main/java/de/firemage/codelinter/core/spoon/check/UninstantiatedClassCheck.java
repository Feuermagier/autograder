package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Check;
import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.TypeFilter;

public class UninstantiatedClassCheck extends AbstractLoggingProcessor<CtClass<?>> {
    public static final String DESCRIPTION = "Class is never instantiated";
    public static final String EXPLANATION = """
            Consider making this class abstract or remove all non-static methods if this class is supposed to be a utility class.""";

    public UninstantiatedClassCheck(Check check) {
        super(check);
    }

    @Override
    public void process(CtClass<?> element) {
        if (!element.isAbstract() && hasNonStaticMethod(element) && isNeverConstructed(element)) {
            addProblem(element, DESCRIPTION);
        }
    }

    private boolean hasNonStaticMethod(CtClass<?> element) {
        return !element.getMethods().stream().allMatch(CtModifiable::isStatic);
    }

    private boolean isNeverConstructed(CtClass<?> element) {
        return Query.getElements(getFactory(), new TypeFilter<>(CtConstructorCall.class)).stream().noneMatch(
                call -> call.getType().equals(element.getReference()));
    }
}
