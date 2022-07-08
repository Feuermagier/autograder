package de.firemage.autograder.core.check_tests.CompareObjectsNotStrings.code;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        Object a = new Object();
        Object b = new Object();
        Foo c = new Foo();
        
        if (a.equals(b)) { // Ok
            System.out.println("Hi");
        }

        if (a.equals(b.toString())) { // Ok...
            System.out.println("Hi");
        }

        if (a.toString().equals(b)) { // Ok...
            System.out.println("Hi");
        }

        if (a.toString().equals(b.toString())) { // Not ok
            System.out.println("Hi");
        }

        if (a.toString().equals(c.toString())) { // Ok, because the types are different
            System.out.println("Hi");
        }
    }
}

class Foo {

}
