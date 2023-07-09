package de.firemage.autograder.core.check_tests.EqualsHashCodeContract.code;

public class Test {
    public static void main(String[] args) {
    }
}

class OnlyEquals { /*@ not ok @*/
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

class OnlyHashCode { /*@ not ok @*/
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

class OnlyComparable implements Comparable<OnlyComparable> { /*@ not ok @*/
    @Override
    public int compareTo(OnlyComparable o) {
        return 0;
    }
}

class ShouldBeOkay implements Comparable<ShouldBeOkay> { /*@ ok @*/
    @Override
    public int compareTo(ShouldBeOkay o) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

class ShouldBeOkayAsWell { /*@ ok @*/
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
