package de.firemage.autograder.core.check_tests.CompareObjectsNotStrings.code;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        Object a = new Object();
        Object b = new Object();
        Foo c = new Foo();
        
        if (a.equals(b)) { /*@ ok @*/
            System.out.println("Hi");
        }

        if (a.equals(b.toString())) { /*@ ok @*/
            System.out.println("Hi");
        }

        if (a.toString().equals(b)) { /*@ ok @*/
            System.out.println("Hi");
        }

        if (a.toString().equals(b.toString())) { /*@ not ok @*/
            System.out.println("Hi");
        }

        if (a.toString().equals(c.toString())) { /*@ ok; because the types are different @*/
            System.out.println("Hi");
        }
    }
}

class Foo {

}
