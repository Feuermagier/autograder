package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class BooleanMethodNameCheck extends PMDCheck {
    private static final String DESCRIPTION =
        "Methods without parameters that return booleans should not have the 'get' prefix but be named 'isXYZ'.";
    private static final String QUERY = """
        //MethodDeclaration
            [starts-with(@Name, 'get')]
            [@Arity = 0]
            [ResultType/Type/PrimitiveType[@Image = 'boolean']]
        """;

    public BooleanMethodNameCheck() {
        super(DESCRIPTION,
            createXPathRule("boolean method naming", "The method should be called isY() instead of getY()", QUERY),
            ProblemType.BOOLEAN_GETTER_NOT_CALLED_IS);
    }
}
