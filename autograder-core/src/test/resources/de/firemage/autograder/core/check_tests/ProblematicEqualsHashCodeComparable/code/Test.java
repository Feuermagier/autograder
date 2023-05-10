package de.firemage.autograder.core.check_tests.ProblematicEqualsHashCodeComparable.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {}

    static <T> boolean isLessThan(Comparator<T> comparator, T a, T b) {
        // Fragile: it's not guaranteed that `comparator` returns -1 to mean
        // "less than".
        return comparator.compare(a, b) == -1; // Not Ok
    }
}

class MyClass { // Not Ok
    private int a;
    private int b;
    private String c;

    @Override
    public boolean equals(Object o) {
        return o.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }
}

class EqualsUnsafeCast { // Not Ok
    private int a;

    @Override
    public boolean equals(Object other) {
        EqualsUnsafeCast that = (EqualsUnsafeCast) other; // BAD: This may throw ClassCastException.
        return a == that.a;
    }
}
