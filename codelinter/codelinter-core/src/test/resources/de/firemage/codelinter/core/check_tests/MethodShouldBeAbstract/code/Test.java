import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

public abstract class Test {
    private Object privateNull() { // Ok, private
        return null;
    }

    public abstract Object abstractMethod(); // Ok, abstract

    public static Object staticNull() { // Ok, static
        return null;
    }
    
    @Override
    public String toString() { // Ok, overrides
        return null;
    }

    private Object privateInstance() { // Ok, private
        return new Object();
    }
    
    public Object publicNull() { // Not ok, returns null
        return null;
    }

    public Object publicNullWithParameters(int x) { // Not ok, returns null
        return null;
    }

    public Object publicInstance() { // Ok, not null
        return new Object();
    }
    
    public void publicDefaultEmpty() { // Not ok, empty
        
    }

    public void publicDefaultEmptyWithParameters(int x) { // Not ok, empty

    }
    
    public void publicThrows() { // Not ok, only throws
        throw new IllegalStateException();
    }

    public void publicThrows2() { // Not ok, only throws
        throw new UnsupportedOperationException();
    }

    public void publicThrowsWithParameters(int x) { // Not ok, only throws
        throw new IllegalStateException();
    }


    public Object publicComplex() { // Ok, does not look like a primitive default
        if (1 < 2) {
            throw new IllegalStateException();
        } else {
            return null;
        }
    }
}
