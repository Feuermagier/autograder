package de.firemage.codelinter.core.check.naming;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class BooleanMethodNameCheck extends PMDCheck {
    private static final String DESCRIPTION = "Methods without parameters that return booleans should not have the 'get' prefix";
    private static final String QUERY = """
            //MethodDeclaration
                [starts-with(@Name, 'get')]
                [@Arity = 0]
                [ResultType/Type/PrimitiveType[@Image = 'boolean']]
            """;

    public BooleanMethodNameCheck() {
        super(DESCRIPTION, createXPathRule("boolean method naming", QUERY));
    }
}
