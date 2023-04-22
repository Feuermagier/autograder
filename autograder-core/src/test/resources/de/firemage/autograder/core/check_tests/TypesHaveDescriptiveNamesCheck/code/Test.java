package de.firemage.autograder.core.check_tests.TypesHaveDescriptiveNamesCheck.code;

public class Test {
    public static void main(String[] args) {}
}

class MyClass {} // Not Ok

enum MyEnum {} // Not Ok

class SomethingWentWrongException extends Exception {} // Ok

class SomethingWentWrong extends Exception {} // Not Ok

class WorldObject {} // Not Ok
