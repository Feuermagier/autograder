package de.firemage.autograder.core.check_tests.ForToForeach.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6);

        for (int i = 0; i < list.size(); i++) { // Should be for-each
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i++) { // Should be for-each
            System.out.println(list.get(i));
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i += 2) { // Ok
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size() - 1; i += 2) { // Ok
            System.out.println(list.get(i));
        }

        for (int i = 0; i < list.size(); i++) { // Ok
            System.out.println(i);
        }

        for (int i : list) { // Ok
            System.out.println(i);
        }
    }
}

class PmdExample1 {
    void loop(List<String> l) {
        for (int i = 0; i < l.size(); i++) { // Not Ok
            System.out.println(l.get(i));
        }
    }
}

class PmdExample2 {
    void loop(List<String> lo) {
        for (int i = 0; i <= lo.size() - 1; i++) { // Not Ok
            System.out.println(lo.get(i));
        }
    }
}

class PmdExample3 {
    void loop(ArrayList<String> l) {
        for (int i = 0; i < l.size(); i++) { // Not Ok
            System.out.println(l.get(i));
        }
    }
}

class Node {
    Node[] children;

    public Object childrenAccept(Object data) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) { // Not Ok
                Node apexNode = (Node) children[i];
                System.out.println(apexNode);
            }
        }
        return data;
    }
}


class PmdExample5 {
    protected static final char[] filter(char[] chars, char removeChar) {
        int count = 0;
        for (int i = 0; i < chars.length; i++) { // Not Ok
            if (chars[i] == removeChar) {
                count++;
            }
        }

        char[] results = new char[chars.length - count];

        int index = 0;
        for (int i = 0; i < chars.length; i++) { // Not Ok
            if (chars[i] != removeChar) {
                results[index++] = chars[i];
            }
        }
        return results;
    }
}

class PmdExample9 {
    void loop(List<String> l) {
        int i = 0;
        for (; i < l.size(); i++) { // Not Ok
            System.out.println(l.get(i));
        }
    }
}

class PmdExample6 {
    void loop(List<String> l) {
        for (int i = 0; i < l.size(); i++) { // Ok
            System.out.println(i + ": " + l.get(i));
        }
    }
}

class PmdExample7 {
    void loop(List<String> l) {
        List<String> l2 = new ArrayList<>(l);
        for (int i = 0; i < l.size(); i++) { // Ok
            System.out.println(l2.get(i));
        }
    }
}

class PmdExample8 {
    void loop(List<String> l) {
        for (int i = l.size() - 1; i > 0; i-= 1) { // Ok
            System.out.println(i + ": " + l.get(i));
        }
    }
}

class PmdExample10 {
    void loop(List<String> filters, StringBuilder builder) {
        for (int i = 1; i < filters.size(); i++) { // Ok
            builder.append(' ');
            builder.append(filters.get(i));
        }
    }
}

class PmdExample11 {
    private static String findOptionalStringValue(String[] args, String name, String defaultValue) {
        for (int i = 0; i < args.length; i++) { // Ok
            if (args[i].equals(name)) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }
}

class PmdExample12 {
    private String newString() {
        int strLength = randomInt(1, 100);

        char[] chars = new char[strLength];
        for (int i = 0; i < chars.length; i++) { // Ok
            chars[i] = randomCharIn("123");
        }
        return new String(chars);
    }

    private int randomInt(int min, int max) {
        return 42;
    }

    private char randomCharIn(String s) {
        return '1';
    }
}

class PmdExample13 {
    private int[] hashes = new int[10];
    public void foo() {
        List<String> stringList = new ArrayList<>();

        this.hashes = new int[stringList.size()];
        for (int i = 0; i < stringList.size(); i++) { // Ok
            this.hashes[i] = stringList.get(i).hashCode();
        }
    }
}

class PmdExample14 {

    final int hashes[] = new int[6];

    public void foo(PmdExample14 other) {
        for (int i = 0; i < hashes.length; i++) { // Ok
            if (this.hashes[i] == other.hashes[i])
                throw new IllegalStateException();
        }
    }
}

class PmdExample15 {
    private void fofo(List<Foo> mList) {
        for (int i = 0; i < mList.size(); i++) { // Ok
            mList.get(i).setIndex(i);
        }
    }
    interface Foo {
        void setIndex(int i);
    }
}
