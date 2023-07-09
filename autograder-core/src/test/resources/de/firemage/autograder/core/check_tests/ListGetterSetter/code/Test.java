package de.firemage.autograder.core.check_tests.ListGetterSetter.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Test {
    private final List<Integer> list = new ArrayList<>();
    private final int[] array = new int[10];
    private final String[][] array2d = new String[2][2];

    public Test() {
        return; /*@ ok @*/
    }

    public static void main(String[] args) {
        Supplier<List<String>> lambda = () -> { return List.of("a"); }; /*@ ok @*/
    }

    public List<Integer> getList1() {
        return this.list; /*@ not ok @*/
    }

    public List<Integer> getList2() {
        return list; /*@ not ok @*/
    }

    public List<Integer> getList3() {
        return new ArrayList<>(this.list); /*@ ok @*/
    }

    public List<Integer> getList4() {
        return Collections.unmodifiableList(this.list); /*@ ok @*/
    }

    public int[] getArray() {
        return this.array;  /*@ not ok @*/
    }

    public String[][] getArray2d() {
        return this.array2d;  /*@ not ok @*/
    }
}

enum Vegetable {
    CARROT, SALAD;
}

enum FieldKind {
    FIELD(new Vegetable[] {Vegetable.CARROT, Vegetable.SALAD});

    private final Vegetable[] possibleVegetables;

    FieldKind(Vegetable[] possibleVegetables) {
        this.possibleVegetables = possibleVegetables;
    }

    public Vegetable[] getPossibleVegetables() {
        return this.possibleVegetables; /*@ not ok @*/
    }
}
