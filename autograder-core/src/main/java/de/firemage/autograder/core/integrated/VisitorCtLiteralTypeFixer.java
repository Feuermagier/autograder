package de.firemage.autograder.core.integrated;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.CtScanner;

public class VisitorCtLiteralTypeFixer extends CtScanner {
    private final Factory factory;

    public VisitorCtLiteralTypeFixer(Factory factory) {
        this.factory = factory;
    }

    @Override
    public <T> void visitCtLiteral(CtLiteral<T> literal) {
        if (literal.getType() == null) {
            literal.setType(this.factory.Type().createReference(literal.getValue().getClass()));
        }

        super.visitCtLiteral(literal);
    }
}
