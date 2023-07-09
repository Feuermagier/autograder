public class Test {
    public static void main(String[] args) {
        if (true) { /*@ not ok @*/
            if (false) {

            }
        }

        if (true) { /*@ not ok @*/
            if (false) {

            }
        } else {

        }

        if (true) {

        } else if (true) { /*@ not ok @*/
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
            foo(); /*@ ok @*/
        }

        if (true) {

        } else if (true) {

        } else {
            foo(); /*@ ok @*/
            if (true) {

            }
        }

        if (true) {

        } else if (true) {

        } else {
            if (true) { /*@ not ok @*/
            }
        }

        if (true) {

        } else if (true) {

        } else {
            if (true) { /*@ not ok @*/
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
        if (x) { /*@ not ok @*/
            if (y) {
            }
        }

        if (x) { /*@ ok @*/
            int z = 5;
            if (y) {
            }
        }

        if (x) { /*@ ok @*/
            if (y) {
            }
            int z = 5;
        }
    }
}
