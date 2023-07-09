package de.firemage.autograder.core.check_tests.MutableEnum.code;

import java.util.*;

public class Test {
    public static void main(String[] args) {}
}

// From: https://stackoverflow.com/q/41199553/7766117
enum Friends { /*@ not ok @*/
    BOB(14),
    ALICE(22);

    private int age;

    private Friends(int age){
        this.age = age;
    }

    public void setAge(int age){
        this.age = age;
    }

    public int getAge(){
        return age;
    }
}

enum MyEnum { /*@ ok; very difficult to decide that this is mutable @*/
    MY_VARIANT;
    private final List<String> strings = new ArrayList<>();

    public void addString(String string) {
        this.strings.add(string);
    }
}
