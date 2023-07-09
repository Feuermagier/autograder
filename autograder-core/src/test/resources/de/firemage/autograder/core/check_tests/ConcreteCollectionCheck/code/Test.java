package de.firemage.autograder.core.check_tests.ConcreteCollectionCheck.code;

import java.util.*;
// placeholder
// placeholder

public class Test {
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>(); /*@ not ok @*/
        List<Integer> list2 = new ArrayList<>(); /*@ ok @*/
        Collection<Integer> list3 = new ArrayList<>(); /*@ ok @*/
        List<HashSet<String>> list4 = new ArrayList<>(); /*@ not ok @*/
        List<List<String>> list5 = new ArrayList<>(); /*@ ok @*/
        HashSet<Integer> set = new HashSet<>(); /*@ not ok @*/
        Set<Integer> set2 = new HashSet<>(); /*@ ok @*/
        HashMap<String, ArrayList<String>> map = new HashMap<>(); /*@ not ok @*/
        Map<String, ArrayList<String>> map2 = new HashMap<>(); /*@ not ok @*/
        Map<String, List<String>> map3 = new HashMap<>(); /*@ ok @*/
        if (list2 instanceof ArrayList<Integer>) { /*@ ok @*/
            var list2c = (ArrayList<Integer>) list2; /*@ ok @*/
            System.out.println("Ok");
        }

        if (list2.getClass().equals(ArrayList.class)) { /*@ ok @*/
            System.out.println("Ok");
        }

        List[] array = new ArrayList[10]; /*@ ok @*/
        ArrayList<String>[] array2; /*@ not ok @*/
        array[0] = new ArrayList<>(); /*@ ok @*/
        var someList = new ArrayList<>(); /*@ ok @*/
        AbstractMap.SimpleEntry<String, String> entry = null; /*@ ok @*/
    }
}

class MyList extends ArrayList<String> {} /*@ ok @*/
abstract class A {
    abstract ArrayList<String> getList(); /*@ not ok @*/
}

class B extends A {
    @Override
    ArrayList<String> getList() { /*@ ok @*/
        return new ArrayList<>();
    }
}

record MyRecord(ArrayList<String> list) {} /*@ not ok @*/
// The following tests are from pmd
// https://github.com/pmd/pmd/blob/eb653967abaa4db387ecae0a4b29cd131fa7e4d5/pmd-java/src/test/resources/net/sourceforge
// /pmd/lang/java/rule/bestpractices/xml/LooseCoupling.xml


@SuppressWarnings("rawtypes")
class PmdTest {
    Set attr1 = new HashSet(); /*@ ok @*/
    HashSet attr2 = new HashSet(); /*@ not ok @*/
    HashMap attr3 = new HashMap(); /*@ not ok @*/
    HashSet foo() { /*@ not ok @*/
        return new HashSet();
    }

    Map getFoo() { /*@ ok @*/
        return new HashMap();
    }

    void foo2() {} /*@ ok @*/
    Set foo4() { /*@ ok @*/
        return new HashSet();
    }

    void foo5(HashMap bar) {} /*@ not ok @*/
    void foo6(Vector bar) {} /*@ not ok @*/
    void foo7(ArrayList bar) {} /*@ not ok @*/
    // false positive with method reference:
    private static final ThreadLocal TREE_CACHE =
        ThreadLocal.withInitial(HashMap::new); /*@ ok @*/
    // with instanceof and cast
    boolean m(Map m) {
        if (m instanceof HashMap) { /*@ ok @*/
            return ((HashMap) m).isEmpty(); /*@ ok @*/
        }
        return false;
    }

    static class MyMap extends HashMap implements Map { /*@ ok @*/
        static Map create() { return null; }
    }

    static class FooInner {
        final Map map1 =
            MyMap.create(); /*@ ok @*/
        final Map[] map2 = new MyMap[5]; /*@ ok @*/
        final Properties map3 = new Properties(); /*@ ok @*/
    }

    static class O extends ArrayList implements List {
        final O map = new O(); /*@ not ok @*/
    }

    final Object o = ArrayList.class; /*@ ok @*/
    final Object o2 =
        new AbstractMap.SimpleEntry<>("", ""); /*@ ok @*/
    static class O3 extends ArrayList {
        {
            O3.super.clear(); /*@ ok @*/
        }

        class Inner {
            {
                O3.this.clear(); /*@ ok @*/
                O3.super.clear(); /*@ ok @*/
            }
        }
    }
}

@SuppressWarnings("rawtypes")
class Foo2 {
    void firstMethod() {}
    void myMethod() {
        class Inner {
            HashSet foo() { /*@ not ok @*/
                return new HashSet();
            }
        }
        Object o = new Object() {
            HashSet foo() { return new HashSet(); } /*@ not ok @*/
        };
    }
    class Nested {
        HashSet foo() { /*@ not ok @*/
            return new HashSet();
        }
    }
}

final class Foo3<A, B extends Collection<A>> { /*@ ok @*/
    private Set<? super B> things; /*@ ok @*/
    class Vector { Vector(String a, String b, int c) {} }

    private Vector vec = new Vector("a", "b", 1); /*@ ok @*/
}

class Street {
    private final TreeMap<Integer, String> carsOnStreet; /*@ not ok @*/
    public Street() {
        this.carsOnStreet = null; /*@ ok @*/
    }
}

////////////////// The check previously crashed on the following tests
class ABC {
    private Stack<Object>[][] foo; /*@ not ok @*/
    void bar() {
        int x = foo[0][0].size();
    }
}
