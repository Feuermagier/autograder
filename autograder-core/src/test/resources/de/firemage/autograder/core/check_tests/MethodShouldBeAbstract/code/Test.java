package de.firemage.autograder.core.check_tests.MethodShouldBeAbstract.code;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

public abstract class Test {
    private Object privateNull() { /*@ ok; private @*/
        return null;
    }

    public abstract Object abstractMethod(); /*@ ok; abstract @*/

    public static Object staticNull() { /*@ ok; static @*/
        return null;
    }
    
    @Override
    public String toString() { /*@ ok; overrides @*/
        return null;
    }

    private Object privateInstance() { /*@ ok; private @*/
        return new Object();
    }
    
    public Object publicNull() { /*@ not ok; returns null @*/
        return null;
    }

    public Object publicNullWithParameters(int x) { /*@ not ok; returns null @*/
        return null;
    }

    public Object publicInstance() { /*@ ok; not null @*/
        return new Object();
    }
    
    public void publicDefaultEmpty() { /*@ not ok; empty @*/
        
    }

    public void publicDefaultEmptyWithParameters(int x) { /*@ not ok; empty @*/

    }
    
    public void publicThrows() { /*@ not ok; only throws @*/
        throw new IllegalStateException();
    }

    public void publicThrows2() { /*@ not ok; only throws @*/
        throw new UnsupportedOperationException();
    }

    public void publicThrowsWithParameters(int x) { /*@ not ok; only throws @*/
        throw new IllegalStateException();
    }


    public Object publicComplex() { /*@ ok; does not look like a primitive default @*/
        if (1 < 2) {
            throw new IllegalStateException();
        } else {
            return null;
        }
    }
}
