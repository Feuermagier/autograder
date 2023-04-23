package de.firemage.autograder.core.check_tests.ShouldBeEnumAttribute.code;

public class Test {
    public static void main(String[] args) {
    }
}

enum Color {
    RED,
    GREEN,
    BLUE;

    @Override
    public String toString() {
        return switch (this) { // Not Ok
            case RED -> "red";
            case GREEN -> "green";
            case BLUE -> "blue";
        };
    }

    public String toString2() {
        return switch (this) { // Not Ok
            case RED -> "red";
            case GREEN -> "green";
            case BLUE -> "blue";
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }


    public String toString3() {
        return switch (this) { // Not Ok
            case RED -> "red";
            case GREEN -> {
                yield "green";
            }
            case BLUE -> {
                yield "blue";
            }
        };
    }

    public String toString4() {
        String value;
        switch (this) { // Not Ok
            case RED: {
                value = "red";
                break;
            }
            case GREEN: {
                value = "green";
                break;
            }
            case BLUE: {
                value = "blue";
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected value: " + this);
            }
        }

        return value;
    }
}
