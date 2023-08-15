package de.firemage.autograder.core.check_tests.UseGuardClauses.code;

public class Test {
    public static void main(String[] args) {
    }

    void run(Timer timer) {
        if (!timer.isEnabled())
            return;

        if (!timer.isValid())
            throw new IllegalArgumentException();

        timer.run();
    }

    void run2(Timer timer) {
        if (timer.isEnabled() && timer.isValid()) {
            timer.run();
        } else if (!timer.isEnabled()) { /*# not ok #*/
            return;
        } else if (!timer.isValid()) { /*# not ok #*/
            throw new IllegalArgumentException();
        }
    }

    void example(boolean a) {
        if (a) { /*# ok #*/
            System.out.println("a");
            return; // early return
        } else {
            System.out.println("b");
            // normal code path
        }
    }

    void isOkay(boolean a, boolean b) {
        if (a) {
            // do something
        } else if (b) {
            // do some other thing
        } else {
            // do some third thing
        }

        // something that is always done
    }

    void withLoop(boolean a, boolean b) {
        while (a) {
            if (b) {
                System.out.println("hello");
            } else { /*# not ok #*/
                continue;
            }
        }
    }

    void bigExample(boolean a, boolean b, boolean c, boolean d) {
        if (a) {
            if (b) {
                if (c) {
                    // do something
                } else if (d) {
                    // do something
                } else { /*# not ok #*/
                    throw new IllegalArgumentException();
                }
            } else { /*# not ok #*/
                throw new IllegalArgumentException();
            }
        } else { /*# not ok #*/
            throw new IllegalArgumentException();
        }
    }
}

class Timer {
    boolean isEnabled() {
        return true;
    }

    boolean isValid() {
        return true;
    }

    void run() {
    }
}
