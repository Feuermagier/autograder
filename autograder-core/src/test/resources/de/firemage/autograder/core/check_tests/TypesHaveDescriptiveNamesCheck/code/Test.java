package de.firemage.autograder.core.check_tests.TypesHaveDescriptiveNamesCheck.code;

public class Test {
    public static void main(String[] args) {}
}

class MyClass {} /*@ not ok @*/
enum MyEnum {} /*@ not ok @*/
class SomethingWentWrongException extends Exception {} /*@ ok @*/
class SomethingWentWrong extends Exception {} /*@ not ok @*/
class WorldObject {} /*@ not ok @*/