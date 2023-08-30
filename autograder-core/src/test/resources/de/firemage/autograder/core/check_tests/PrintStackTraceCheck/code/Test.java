package de.firemage.autograder.core.check_tests.PrintStackTraceCheck.code;

public class Test {
    public static void main(String[] args) {
        try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace(); /*# not ok #*/
        }

        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println(e); /*# ok #*/
        }
    }
}


class Command<T> {
    protected final T value;

    protected Command(T value) {
        this.value = value;
    }
}

class ResultsInCrash extends Command<Integer> {
    ResultsInCrash(Integer value) {
        super(value);
    }

    public String execute() {
        return value.toString();
    }
}
