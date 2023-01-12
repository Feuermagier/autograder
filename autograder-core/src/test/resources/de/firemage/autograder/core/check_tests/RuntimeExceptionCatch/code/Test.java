public class Test {
    public static void main(String[] args) {
        try {
            System.out.println("Hi");
        } catch (RuntimeException ex) { // Not ok
            
        }
        
        try {
            Integer.parseInt("foo");
        } catch (NumberFormatException ex) { // Ok
            
        } catch (IllegalArgumentException ex) { // Not ok
            
        }
        
        try {
            foo();
        } catch (StackOverflowError error) { // Not ok

        } catch (TextException ex) { // Ok
            
        }
    }
    
    private static void foo() throws TextException {
        throw new TextException();
    }
}

class TextException extends Exception {
    
}
