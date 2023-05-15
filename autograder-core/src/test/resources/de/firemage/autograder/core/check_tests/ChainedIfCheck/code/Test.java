public class Test {
    public static void main(String[] args) {
        if (true) {
            if (false) { // OK
                
            }
        }

        if (true) {
            if (false) { // OK

            }
        } else {
            
        }

        if (true) {
            
        } else if (true) {
            if (false) { // OK

            }
        }

        if (true) {

        } else if (true) {
            
        } else {
            
        }

        if (true) {

        } else if (true) {

        } else {
            foo(); // Ok
        }

        if (true) {

        } else if (true) {

        } else {
            foo(); // Ok
            if (true) {
                
            }
        }

        if (true) {

        } else if (true) {

        } else {
            if (true) { // Not ok

            }
        }

        if (true) {

        } else if (true) {

        } else {
            if (true) { // Not ok

            } else {
                
            }
        }
    }
    
    private static void foo() {
        
    }
}
