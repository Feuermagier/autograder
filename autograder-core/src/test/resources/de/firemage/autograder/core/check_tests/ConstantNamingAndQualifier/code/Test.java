package de.firemage.autograder.core.check_tests.ConstantNamingAndQualifier.code;

import java.util.List;

public class Test {
    private static final int VALUE_A = 1; /*@ ok @*/
    private static final int fooBar = 1; /*@ not ok @*/
    private static final int FooBar = 1; /*@ not ok @*/
    private final int CONSTANT = 1; /*@ not ok @*/
    private final int otherConstant = 1; /*@ not ok @*/
    private final int f; /*@ ok; because initialized in constructor @*/

    private static final String weee = "foo"; /*@ not ok @*/
    private final String heyHey; /*@ ok @*/
    private static final Object d = null; /*@ ok; because not primitive or string @*/

    private static final long serialVersionUID = -2338626292552177485L; /*@ ok; because of serialVersionUID @*/

    public Test() {
        this.f = -1;
        this.heyHey = "Hey";

        final int numberOfThingsToDo = 4; /*@ not ok @*/
        final String someValue = heyHey.substring(0); /*@ ok; because not a literal @*/
    }
}
