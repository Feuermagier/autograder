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
        foo.isEmpty(); /*# ok #*/
        foo.isBlank(); /*# ok #*/
        foo.equals(""); /*# not ok #*/
        foo.equals("foo"); /*# ok #*/
        foo.equals(null); /*# ok #*/
        var a = foo.length() == 0; /*# not ok #*/
        var b = foo.length() == 1; /*# ok #*/
        var c = foo.length() >= 0; /*# ok #*/
        var d = foo.length() >= 1; /*# not ok #*/
        var e = foo.length() > 1; /*# ok #*/
        var f = foo.length() > 0; /*# not ok #*/
        var g = 1 < foo.length(); /*# ok #*/
        var h = 0 < foo.length(); /*# not ok #*/
        var i = 0 <= foo.length(); /*# ok #*/
        var j = 1 <= foo.length(); /*# not ok #*/
        var k = "".equals("hello"); /*# not ok #*/
    }
}
