package de.firemage.autograder.core.check_tests.UseEntrySet.code;

import java.util.*;

enum Color { RED, GREEN, BLUE; }

public class Test {
    public static void main(String[] args) {
        Map<Color, Integer> storedColors = new HashMap<>();
        Map<Color, Integer> result = new HashMap<>();

        for (Color color : storedColors.keySet()) { /*# not ok #*/
            if (storedColors.get(color) > 0) {
                result.put(color, storedColors.get(color));
            }
        }
    }
}

// Test taken from https://github.com/SonarSource/sonar-java/blob/4c1473548b7e2e9fe1da212955f532c6fbc64d18/java-checks-test-sources/src/main/java/checks/KeySetInsteadOfEntrySet.java#L3
class KeySetInsteadOfEntrySetCheck extends java.util.HashMap<String, Object> {
    KeySetInsteadOfEntrySetCheck inner;
    java.util.HashMap<String, Object> map;
}

class KeySetInsteadOfEntrySetCheckExtendedClass extends KeySetInsteadOfEntrySetCheck {

    public class InnerClass {
        InnerClass inner;
        java.util.HashMap<String, Object> map;
    }

    private String key;

    private String getKey() {
        return key;
    }

    private java.util.List<String> list;

    private java.util.List<String> list() {
        return list;
    }

    java.util.HashMap<String, Object> map, map2;
    java.util.HashMap<String, Object>[] map3;

    InnerClass inner;

    public void method() {
        for (String value : list) { /*# ok #*/
        }
        for (String value : list()) { /*# ok #*/
        }
        for (String value : new String[] {}) { /*# ok #*/
        }
        for (String key2 : map.keySet()) { /*# ok #*/
            Object value1 = map.get(key);
            Object value2 = map.get(getKey());
            Object value3 = map2.get(key2);
        }
        for (String key5 : this.map3[0].keySet()) { /*# not ok #*/
            Object value = this.map3[0].get(key5);
        }
        for (String key5 : inner.inner.map.keySet()) { /*# not ok #*/
            Object value = inner.inner.map.get(key5);
        }
        for (String key5 : keySet()) { /*# ok #*/
            Object value = map3[0].get(key5);
        }
        for (String key5 : super.inner.map.keySet()) { /*# not ok #*/
            Object value = super.inner.map.get(key5);
        }
        for (String key5 : this.inner.map.keySet()) { /*# not ok #*/
            Object value = this.inner.map.get(key5);
        }
        for (java.util.Map.Entry<String, Object> key6 : map.entrySet()) { /*# ok #*/
            Object value = map.get(key6);
        }

        for (String key3 : keySet()) { /*# not ok #*/
            Object value = get(key3);
        }
        for (String key4 : this.keySet()) { /*# not ok #*/
            Object value = this.get(key4);
        }
        for (String key5 : super.keySet()) { /*# not ok #*/
            Object value = super.get(key5);
        }
        for (String key5 : map.keySet()) { /*# not ok #*/
            Object value = map.get(key5);
        }
        for (String key5 : super.map.keySet()) { /*# not ok #*/
            Object value = super.map.get(key5);
        }
        for (String key5 : super.map.keySet()) { /*# ok #*/
            Object value = this.map.get(key5);
        }
        for (String key5 : this.map.keySet()) { /*# not ok #*/
            Object value = this.map.get(key5);
        }
    }

}
