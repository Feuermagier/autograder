package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.check.Check;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;

public class MagicStringCheck extends AbstractLoggingProcessor<CtLiteral<String>> {
    private static final String DESCRIPTION = "Magic string";
    private static final String EXPLANATION = """
            Declare that string in a private static final field and reference it here.""";

    private final boolean allowExceptionDescriptions;

    public MagicStringCheck(Check check, boolean allowExceptionDescriptions) {
        super(check);
        this.allowExceptionDescriptions = allowExceptionDescriptions;
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
            if (this.allowExceptionDescriptions) {
                if (parent instanceof CtConstructorCall && CheckUtil.isException(((CtConstructorCall<?>) parent).getType())) {
                    return;
                }
            }

            addProblem(literal, DESCRIPTION);
        }
    }
}
