package de.firemage.autograder.core.check_tests.UtilityClassCheck.code;

public class Test {} // Ok

final class UtilityClass { // Ok
    private UtilityClass() {
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}

    public static void b() {}
}


final class WithoutPrivateConstructor { // Not Ok
    public static void a() {}
}

class WithPrivateConstructorButNotFinal { // Not Ok
    private WithPrivateConstructorButNotFinal() {}

    public static void a() {}
}

class WithPublicConstructor { // Not Ok
    public WithPublicConstructor() {}

    public static void a() {}
}

class WithInnerClass { // Ok
    private final String a;
    private final String b;

    public WithInnerClass(String a, String b) {
        this.a = a;
        this.b = b;
    }

    public static class InnerClass { // Ok (will be ignored for simplicity)
        public static void a() {}
    }
}

// see https://github.com/Feuermagier/autograder/issues/83
class MyException extends Exception { // Ok
    public MyException(String message) {
        super(updateMessage(message));
    }

    private static String updateMessage(String message) {
        return "Error: " + message;
    }
}

final class MyUtilityClassWithNonPrivate {
    protected MyUtilityClassWithNonPrivate() {  // Not Ok
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}
    public static void b() {}
}

abstract class MyAbstractUtilityClassWithNonPrivate {
    protected MyAbstractUtilityClassWithNonPrivate() { // Not Ok
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}
    public static void b() {}
}

final class MyUtilityClassWithNonPackagePrivate {
    MyUtilityClassWithNonPackagePrivate() { // Not Ok
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}
    public static void b() {}
}
