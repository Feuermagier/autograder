package de.firemage.autograder.core.check_tests.OptionalBadPractices.code;

import java.util.Optional;

public class Test {
    private Optional<String> opt1; // Ok

    public Test(Optional<String> opt1) { // Not Ok
        this.opt1 = opt1;
    }

    public static void main(String[] args) {
        Optional<String> optional = Optional.empty(); // Ok
        Optional<Boolean> optionalTriState = Optional.of(true); // Not Ok
    }

    public Optional<String> getOpt1() { // Ok
        return Optional.empty();
    }

    public Optional<Boolean> getOpt2() { // Not Ok
        return Optional.empty();
    }

    public Optional<String> setOpt3(Optional<Integer> a) { // Not Ok
        return Optional.empty();
    }
}
