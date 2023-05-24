package de.firemage.autograder.core.check_tests.ConstantNamingAndQualifier.code;

import java.util.List;

public class Test {
    private static final int VALUE_A = 1; // Ok
    private static final int fooBar = 1; // Not ok
    private static final int FooBar = 1; // Not ok
    private final int CONSTANT = 1; // Not ok
    private final int otherConstant = 1; // Not ok
    private final int f; // Ok, because initialized in constructor

    private static final String weee = "foo"; // Not ok
    private final String heyHey; // Ok
    private static final Object d = null; // Ok, because not primitive or string

    private static final long serialVersionUID = -2338626292552177485L; // Ok, because of serialVersionUID

    public Test() {
        this.f = -1;
        this.heyHey = "Hey";

        final int numberOfThingsToDo = 4; // Not Ok

        final String someValue = heyHey.substring(0); // Ok, because not a literal
    }
}
