package de.firemage.autograder.core.check_tests.InterfaceBadPractices.code;

public class Test { /*@ not ok @*/
    public static final String A = "";
    public static final String B = "";
    public static final String C = "";
    public static final String D = "";
    public static final String E = "";
    public static final String F = "";
    public static final String G = "";
    public static final String H = "";
    public static final String I = "";
    public static final String J = "";
    public static final String K = "";
    public static final String L = "";
    public static final String M = "";
    public static final String N = "";
    public static final String O = "";

    public static void main(String[] args) {}
}

enum ConstantsEnum { /*@ not ok @*/
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

class EverythingPrivate { /*@ ok @*/
    private static final String ARGUMENT_NO_AI = "pvp";
    private static final String ARGUMENT_AI = "ki";
    private static final int MAX_ARGS_SIZE = 4;
    private static final int MINIMUM_BOARD_SIZE = 3;
    private static final int MAXIMUM_BOARD_SIZE = 9;
    private static final int MINIMUM_WIN_REQUIREMENT = 3;
    private static final int MAXIMUM_WIN_REQUIREMENT = 9;
    private static final int WIN_REQUIREMENT_ARGS_INDEX = 2;
    private static final int MINIMUM_MAX_AMOUNT_ENTRIES = 9;
    private static final int MAXIMUM_MAX_AMOUNT_ENTRIES = 81;
    private static final int MAX_AMOUNT_ARGS_INDEX = 3;

    public static void main(String[] args) {}
}
