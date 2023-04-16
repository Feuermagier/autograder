package de.firemage.autograder.core.check_tests.Assert.code;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
    }

    public static Map getRawMap() {
        Map map = new HashMap();
        map.put("key", "value");
        return map;
    }

    public static void makeUnsafeTypeCast() {
        Map<String, String> map = (Map<String, String>) getRawMap(); // Not Ok
    }
}

class Array<T> {
    private T[] array;

    Array(int size) {
        this.array = (T[]) new Object[size]; // Not Ok
    }
}

@SuppressWarnings("unchecked")
class ShouldIgnoreSuppressWarnings {
    private Map<String, String> map;

    ShouldIgnoreSuppressWarnings() {
        this.map = (Map<String, String>) new HashMap(); // Not Ok
    }
}
