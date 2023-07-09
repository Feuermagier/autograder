package de.firemage.autograder.core.check_tests.UtilityClassCheck.code;

public class Test {} /*@ ok @*/
final class UtilityClass { /*@ ok @*/
    private UtilityClass() {
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}

    public static void b() {}
}


final class WithoutPrivateConstructor { /*@ not ok @*/
    public static void a() {}
}

class WithPrivateConstructorButNotFinal { /*@ not ok @*/
    private WithPrivateConstructorButNotFinal() {}

    public static void a() {}
}

class WithPublicConstructor { /*@ not ok @*/
    public WithPublicConstructor() {} /*@ not ok @*/
    public static void a() {}
}

class WithInnerClass { /*@ ok @*/
    private final String a;
    private final String b;

    public WithInnerClass(String a, String b) {
        this.a = a;
        this.b = b;
    }

    public static class InnerClass { //?# ok (will be ignored for simplicity)
        public static void a() {}
    }
}

// see https://github.com/Feuermagier/autograder/issues/83
class MyException extends Exception { /*@ ok @*/
    public MyException(String message) {
        super(updateMessage(message));
    }

    private static String updateMessage(String message) {
        return "Error: " + message;
    }
}

final class MyUtilityClassWithNonPrivate {
    protected MyUtilityClassWithNonPrivate() {  /*@ not ok @*/
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}
    public static void b() {}
}

abstract class MyAbstractUtilityClassWithNonPrivate {
    protected MyAbstractUtilityClassWithNonPrivate() { /*@ not ok @*/
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}
    public static void b() {}
}

final class MyUtilityClassWithNonPackagePrivate {
    MyUtilityClassWithNonPackagePrivate() { /*@ not ok @*/
        throw new IllegalStateException("Utility class");
    }

    public static void a() {}
    public static void b() {}
}
