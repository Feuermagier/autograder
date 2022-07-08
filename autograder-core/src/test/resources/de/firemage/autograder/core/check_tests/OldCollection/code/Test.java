package de.firemage.autograder.core.check_tests.OldCollection.code;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

public class Test {
    public static void main(String[] args) {
        new ArrayList<String>(); // Ok
        new LinkedList<String>(); // Ok
        new Vector<String>(); // Not Ok
        new ArrayDeque<String>(); // Ok
        new Stack<String>(); // Not Ok
        new HashMap<String, String>(); // Ok
        new TreeMap<String, String>(); // Ok
        new Hashtable<String, String>(); // Not Ok
    }
}
