package de.firemage.autograder.core.check_tests.InheritanceBadPractices.code;

public class Test {}


abstract class ExampleParent { /*@ not ok; (composition over inheritance) @*/
    private String field;
    public ExampleParent() { /*@ not ok @*/
        this.field = "test";
    }
}

class Subclass extends ExampleParent {
    public Subclass() {
        super();
    }
}


abstract class ToDisplay { /*@ not ok @*/
    public abstract String toDisplay();
}

class Lake extends ToDisplay {
    @Override
    public String toDisplay() {
        return "Lake";
    }
}
