package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class DiamondOperatorCheck extends PMDCheck {
    private static final LocalizedMessage DESCRIPTION = new LocalizedMessage("diamond-desc");
    
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
        super(DESCRIPTION, createXPathRule("diamond operator", "diamond-exp", QUERY),
            ProblemType.UNUSED_DIAMOND_OPERATOR);
    }
}
