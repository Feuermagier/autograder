package de.firemage.autograder.core.check_tests.InterfaceBadPractices.code;

public class Test { // Not Ok
    private static final String A = "";
    private static final String B = "";
    private static final String C = "";
    private static final String D = "";
    private static final String E = "";
    private static final String F = "";
    private static final String G = "";
    private static final String H = "";
    private static final String I = "";
    private static final String J = "";
    private static final String K = "";
    private static final String L = "";
    private static final String M = "";
    private static final String N = "";
    private static final String O = "";

    public static void main(String[] args) {}
}

enum ConstantsEnum { // Not Ok
    A(""),
    B(""),
    C(""),
    D(""),
    E(""),
    F(""),
    G(""),
    H(""),
    I(""),
    J(""),
    K(""),
    L(""),
    M(""),
    N(""),
    O("");

    private final String value;

    ConstantsEnum(String value) {
        this.value = value;
    }
}
