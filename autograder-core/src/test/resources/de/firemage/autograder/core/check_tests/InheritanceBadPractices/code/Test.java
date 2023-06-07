package de.firemage.autograder.core.check_tests.InheritanceBadPractices.code;

public class Test {}


abstract class ExampleParent { // Not Ok (composition over inheritance)
    private String field;
    public ExampleParent() { // Not Ok
        this.field = "test";
    }
}

class Subclass extends ExampleParent {
    public Subclass() {
        super();
    }
}


abstract class ToDisplay { // Not Ok
    public abstract String toDisplay();
}

class Lake extends ToDisplay {
    @Override
    public String toDisplay() {
        return "Lake";
    }
}
