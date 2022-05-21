package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.check.Check;
import spoon.reflect.code.CtLocalVariable;

public class VarProcessor extends AbstractLoggingProcessor<CtLocalVariable<?>> {
    private static final String DESCRIPTION = "Used 'var'";
    private static final String EXPLANATION = """
            'var' is not inherently bad, but should be avoided in the context of programming lectures.
            Many reviewers consider type inference using 'var' to be bad practice.
            'var' hides type information and is only useful to shorten long, nested generic types.
            Therefore it should never be used to alias primitive types or simple class names.""";

    public VarProcessor(Check check) {
        super(check);
    }

    @Override
    public void process(CtLocalVariable<?> element) {
        if (element.isInferred()) {
            addProblem(element, DESCRIPTION);
        }
    }
}
