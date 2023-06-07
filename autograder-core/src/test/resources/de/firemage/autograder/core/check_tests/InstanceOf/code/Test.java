package de.firemage.autograder.core.check_tests.InstanceOf.code;

public class Test {
    public static void main(String[] args) {}
}

class A {}

class B extends A {
    void foo() {
        A value = new B();
        if (value instanceof B) { // Not Ok
            // do something
        }

        if (value.getClass().equals(B.class)) { // Not Ok
            // do something
        }

        try {
            B b = (B) value;
            // do something
        } catch (ClassCastException ignored) { // Not Ok
            // not of type B
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof B; // Ok
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName(); // Ok
    }
}
