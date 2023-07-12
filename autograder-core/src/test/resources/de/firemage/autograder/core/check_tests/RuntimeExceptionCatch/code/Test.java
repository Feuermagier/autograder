public class Test {
    public static void main(String[] args) {
        try {
            System.out.println("Hi");
        } catch (RuntimeException ex) { /*# not ok #*/
        }
        
        try {
            Integer.parseInt("foo");
        } catch (NumberFormatException ex) { /*# ok #*/
        } catch (IllegalArgumentException ex) { /*# not ok #*/
        }
        
        try {
            foo();
        } catch (StackOverflowError error) { /*# not ok #*/
        } catch (TextException ex) { /*# ok #*/
        }
    }
    
    private static void foo() throws TextException {
        throw new TextException();
    }
}

class TextException extends Exception {
    
}
