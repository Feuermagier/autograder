package de.firemage.autograder.core.check_tests.ConcreteCollectionCheck.code;

import java.util.*;
// placeholder
// placeholder

public class Test {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>(); // Not Ok
        List<Integer> list2 = new ArrayList<>(); // Ok
        Collection<Integer> list3 = new ArrayList<>(); // Ok
        List<HashSet<String>> list4 = new ArrayList<>(); // Not Ok
        List<List<String>> list5 = new ArrayList<>(); // Ok

        HashSet<Integer> set = new HashSet<>(); // Not Ok
        Set<Integer> set2 = new HashSet<>(); // Ok

        HashMap<String, ArrayList<String>> map = new HashMap<>(); // Not Ok
        Map<String, ArrayList<String>> map2 = new HashMap<>(); // Not Ok
        Map<String, List<String>> map3 = new HashMap<>(); // Ok

        if (list2 instanceof ArrayList<Integer>) { // Ok
            var list2c = (ArrayList<Integer>) list2; // Ok
            System.out.println("Ok");
        }

        if (list2.getClass().equals(ArrayList.class)) { // Ok
            System.out.println("Ok");
        }

        List[] array = new ArrayList[10]; // Ok
        ArrayList<String>[] array2; // Not Ok
        array[0] = new ArrayList<>(); // Ok

        AbstractMap.SimpleEntry<String, String> entry = null; // Ok
    }
}

class MyList extends ArrayList<String> {} // Ok

abstract class A {
    abstract ArrayList<String> getList(); // Not Ok
}

class B extends A {
    @Override
    ArrayList<String> getList() { // Ok
        return new ArrayList<>();
    }
}

record MyRecord(ArrayList<String> list) {} // Not Ok

// The following tests are from pmd
// https://github.com/pmd/pmd/blob/eb653967abaa4db387ecae0a4b29cd131fa7e4d5/pmd-java/src/test/resources/net/sourceforge
// /pmd/lang/java/rule/bestpractices/xml/LooseCoupling.xml


@SuppressWarnings("rawtypes")
class PmdTest {
    Set attr1 = new HashSet(); // Ok
    HashSet attr2 = new HashSet(); // Not Ok
    HashMap attr3 = new HashMap(); // Not Ok

    HashSet foo() { // Not Ok
        return new HashSet();
    }

    Map getFoo() { // Ok
        return new HashMap();
    }

    void foo2() {} // Ok

    Set foo4() { // Ok
        return new HashSet();
    }

    void foo5(HashMap bar) {} // Not Ok
    void foo6(Vector bar) {} // Not Ok
    void foo7(ArrayList bar) {} // Not Ok

    // false positive with method reference:
    private static final ThreadLocal TREE_CACHE =
        ThreadLocal.withInitial(HashMap::new); // Ok

    // with instanceof and cast
    boolean m(Map m) {
        if (m instanceof HashMap) { // Ok
            return ((HashMap) m).isEmpty(); // Ok
        }
        return false;
    }

    static class MyMap extends HashMap implements Map { // Ok
        static Map create() { return null; }
    }

    static class FooInner {
        final Map map1 =
            MyMap.create(); // Ok

        final Map[] map2 = new MyMap[5]; // Ok
        final Properties map3 = new Properties(); // Ok
    }

    static class O extends ArrayList implements List {
        final O map = new O(); // Not Ok
    }

    final Object o = ArrayList.class; // Ok
    final Object o2 =
        new AbstractMap.SimpleEntry<>("", ""); // Ok

    static class O3 extends ArrayList {
        {
            O3.super.clear(); // Ok
        }

        class Inner {
            {
                O3.this.clear(); // Ok
                O3.super.clear(); // Ok
            }
        }
    }
}

@SuppressWarnings("rawtypes")
class Foo2 {
    void firstMethod() {}
    void myMethod() {
        class Inner {
            HashSet foo() { // Not Ok
                return new HashSet();
            }
        }
        Object o = new Object() {
            HashSet foo() { return new HashSet(); } // Not Ok
        };
    }
    class Nested {
        HashSet foo() { // Not Ok
            return new HashSet();
        }
    }
}

final class Foo3<A, B extends Collection<A>> { // Ok
    private Set<? super B> things; // Ok

    class Vector { Vector(String a, String b, int c) {} }

    private Vector vec = new Vector("a", "b", 1); // Ok
}
