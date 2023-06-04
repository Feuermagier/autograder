public class Test {
    public static void main(String[] args) {
        if (true) { // Not Ok
            if (false) {

            }
        }

        if (true) { // Not Ok
            if (false) {

            }
        } else {

        }

        if (true) {

        } else if (true) { // Not Ok
            if (false) {

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
