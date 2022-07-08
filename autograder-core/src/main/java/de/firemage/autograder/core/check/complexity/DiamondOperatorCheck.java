package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.pmd.PMDCheck;

public class DiamondOperatorCheck extends PMDCheck {
    private static final String DESCRIPTION = "Use the 'diamond operator' instead of repeating the generic type: new Foo<>()";
    private static final String QUERY = """
            (
            //VariableInitializer[preceding-sibling::VariableDeclaratorId[1]/@TypeInferred=false()]
            |
            //StatementExpression[AssignmentOperator and PrimaryExpression/PrimaryPrefix[not(Expression)]]
            )
            /(Expression | Expression/ConditionalExpression | Expression/ConditionalExpression/Expression)
            /PrimaryExpression[not(PrimarySuffix) and not(ancestor::ArgumentList)]
            /PrimaryPrefix
            /AllocationExpression
                [@AnonymousClass=false()]
                [ClassOrInterfaceType/TypeArguments[@Diamond=false()]]
                [not(ArrayDimsAndInits)]
            """;

    public DiamondOperatorCheck() {
        super(DESCRIPTION, createXPathRule("diamond operator", "Use the 'diamond operator'", QUERY));
    }
}
