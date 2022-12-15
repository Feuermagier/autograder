package de.firemage.autograder.core.check.naming;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.pmd.PMDCheck;

public class BooleanMethodNameCheck extends PMDCheck {
    private static final String QUERY = """
        //MethodDeclaration
            [starts-with(@Name, 'get')]
            [@Arity = 0]
            [ResultType/Type/PrimitiveType[@Image = 'boolean']]
        """;

    public BooleanMethodNameCheck() {
        super(new LocalizedMessage("bool-getter-name-desc"),
            createXPathRule("boolean method naming", "bool-getter-name-exp", QUERY),
            ProblemType.BOOLEAN_GETTER_NOT_CALLED_IS);
    }
}
