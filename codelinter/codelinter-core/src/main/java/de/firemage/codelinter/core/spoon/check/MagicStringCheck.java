package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.ProblemCategory;
import de.firemage.codelinter.core.ProblemPriority;
import de.firemage.codelinter.core.spoon.ProblemLogger;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;

public class MagicStringCheck extends AbstractLoggingProcessor<CtLiteral<String>> {
    private static final String DESCRIPTION = "Magic string";
    private static final String EXPLANATION = """
            Declare that string in a private static final field and reference it here.""";


    public MagicStringCheck(ProblemLogger logger) {
        super(logger);
    }

    @Override
    public void process(CtLiteral<String> literal) {
        if (!literal.getType().getQualifiedName().equals("java.lang.String")) {
            return;
        }
        if (!literal.getValue().isEmpty() && !(literal.getParent() instanceof CtField)) {
            CtElement parent = literal.getParent();
            if (parent instanceof  CtField) {
                // This is a field; the string is probably fine
                return;
            }
            if (parent instanceof CtConstructorCall constructor && CheckUtil.isException(((CtConstructorCall<?>) parent).getType())) {
                // We allow to pass a literal string to an exception constructor
                return;
            }
            addProblem(literal, DESCRIPTION, ProblemCategory.BAD_STYLE, EXPLANATION, ProblemPriority.FIX_RECOMMENDED);
        }
    }
}
