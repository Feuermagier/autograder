package de.firemage.autograder.core.check_tests.ListGetterSetter.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Test {
    private final List<Integer> list = new ArrayList<>();

    public static void main(String[] args) {

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
