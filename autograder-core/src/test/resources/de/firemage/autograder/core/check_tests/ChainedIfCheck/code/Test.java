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

// Tests from https://github.com/pmd/pmd/blob/e8dbb54cb5ed682d9dfd4f54895f4bbd73be728e/pmd-java/src/test/resources/net/sourceforge/pmd/lang/java/rule/design/xml/CollapsibleIfStatements.xml#L4
class PmdTests {
    void callable(boolean x, boolean y) {
        if (x) { // Not Ok
            if (y) {
            }
        }

        if (x) { // Ok
            int z = 5;
            if (y) {
            }
        }

        if (x) { // Ok
            if (y) {
            }
            int z = 5;
        }
    }
}
