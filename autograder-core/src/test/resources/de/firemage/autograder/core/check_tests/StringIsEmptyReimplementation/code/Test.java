package de.firemage.autograder.core.check_tests.StringIsEmptyReimplementation.code;

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
        String foo = "";
        foo.isEmpty(); // Ok
        foo.isBlank(); // Ok
        foo.equals(""); // Not Ok
        foo.equals("foo"); // Ok
        foo.equals(null); // Ok
        var a = foo.length() == 0; // Not Ok
        var b = foo.length() == 1; // Ok
        var c = foo.length() >= 0; // Ok
        var d = foo.length() >= 1; // Not Ok
        var e = foo.length() > 1; // Ok
        var f = foo.length() > 0; // Not Ok
        var g = 1 < foo.length(); // Ok
        var h = 0 < foo.length(); // Not Ok
        var i = 0 <= foo.length(); // Ok
        var j = 1 <= foo.length(); // Not Ok
        var k = "".equals("hello"); // Not Ok
    }
}
