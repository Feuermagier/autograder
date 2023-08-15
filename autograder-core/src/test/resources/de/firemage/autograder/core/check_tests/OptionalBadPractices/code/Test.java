package de.firemage.autograder.core.check_tests.OptionalBadPractices.code;

import java.util.Optional;

public class Test {
    private Optional<String> opt1; /*# ok #*/

    public Test(Optional<String> opt1) { /*# not ok #*/
        this.opt1 = opt1;
    }

    public static void main(String[] args) {
        Optional<String> optional = Optional.empty(); /*# ok #*/
        Optional<Boolean> optionalTriState = Optional.of(true); /*# not ok #*/
    }

    public Optional<String> getOpt1() { /*# ok #*/
        return Optional.empty();
    }

    public Optional<Boolean> getOpt2() { /*# not ok #*/
        return Optional.empty();
    }

    public Optional<String> setOpt3(Optional<Integer> a) { /*# not ok #*/
        return Optional.empty();
    }
}
