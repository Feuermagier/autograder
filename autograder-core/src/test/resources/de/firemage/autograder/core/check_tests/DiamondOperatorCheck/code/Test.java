package de.firemage.autograder.core.check_tests.DiamondOperatorCheck.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>(); // Not Ok
    }
}

// https://github.com/Feuermagier/autograder/issues/125
class ReproduceIssueInReturn {
    static class Pair<L, U> {
        public Pair(L l, U u) {}
    }

    enum VegetableType {
        TOMATO,
        POTATO,
        CARROT
    }


    Pair<VegetableType, Integer> makePair(VegetableType vegetable, int price) {
        return new Pair<VegetableType, Integer>(vegetable, price); // Not Ok
    }
}

// https://github.com/Feuermagier/autograder/issues/114
class ReproduceSuperCallIssue {
    enum A {
        B, C;
    }

    class C {
        C(List<A> list) {
            // ...
        }
    }

    class F extends C {
        F() {
            super(new ArrayList<A>(List.of(A.B, A.C))); // Not Ok
        }
    }
}
