package de.firemage.autograder.core.check_tests.UnreachableCode.code;

public class Test {
    private Test() {  // Not Ok
        throw new IllegalArgumentException("unreachable");
    }

    public static void main(String[] args) {}
}

class Factory {
    private Factory() { // Ok
        throw new IllegalArgumentException("unreachable");
    }

    public static Factory create() {
        return new Factory();
    }
}
