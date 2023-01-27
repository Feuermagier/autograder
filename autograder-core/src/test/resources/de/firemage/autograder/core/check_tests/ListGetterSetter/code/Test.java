package de.firemage.autograder.core.check_tests.ListGetterSetter.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class Test {
    private final List<Integer> list = new ArrayList<>();

    public Test() {
        return; // Ok
    }

    public static void main(String[] args) {
        Supplier<List<String>> lambda = () -> { return List.of("a"); }; // Ok
    }

    public List<Integer> getList1() {
        return this.list;
    }

    public List<Integer> getList2() {
        return list;
    }

    public List<Integer> getList3() {
        return new ArrayList<>(this.list);
    }

    public List<Integer> getList4() {
        return Collections.unmodifiableList(this.list);
    }
}
