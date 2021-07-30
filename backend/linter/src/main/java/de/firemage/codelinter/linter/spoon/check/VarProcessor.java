package de.firemage.codelinter.linter.spoon.check;

import de.firemage.codelinter.linter.spoon.InCodeProblem;
import de.firemage.codelinter.linter.spoon.ProblemCategory;
import de.firemage.codelinter.linter.spoon.ProblemLogger;
import de.firemage.codelinter.linter.spoon.SpoonInCodeProblem;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;

public class VarProcessor extends AbstractLoggingProcessor<CtLocalVariable<?>> {
    private static final String DESCRIPTION = "Used 'var'";
    private static final String EXPLANATION = """
            'var' is not inherently bad, but should be avoided in the context of programming lectures.
            Many reviewers consider type inference using 'var' to be bad practice.
            'var' hides type information and is only useful to shorten long, nested generic types.
            Therefore it should never be used to alias primitive types or simple class names.""";

    public VarProcessor(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtLocalVariable<?> element) {
        if (element.isInferred()) {
            addProblem(new SpoonInCodeProblem(element, DESCRIPTION, ProblemCategory.JAVA_FEATURE, EXPLANATION));
        }
    }
}
