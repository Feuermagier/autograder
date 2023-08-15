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
        new ArrayList<String>(); /*# ok #*/
        new LinkedList<String>(); /*# ok #*/
        new Vector<String>(); /*# not ok #*/
        new ArrayDeque<String>(); /*# ok #*/
        new Stack<String>(); /*# not ok #*/
        new HashMap<String, String>(); /*# ok #*/
        new TreeMap<String, String>(); /*# ok #*/
        new Hashtable<String, String>(); /*# not ok #*/
    }
}
