package de.firemage.codelinter.linter.compiler;

import lombok.Getter;

public enum JavaVersion {
    JAVA_7("7", 7),
    JAVA_8("8", 8),
    JAVA_9("9", 9),
    JAVA_10("10", 10),
    JAVA_11("11", 11),
    JAVA_12("12", 12),
    JAVA_13("13", 13),
    JAVA_14("14", 14),
    JAVA_15("15", 15),
    JAVA_16("16", 16),
    JAVA_17("17", 17);

    @Getter
    private final String versionString;

    @Getter
    private final int versionNumber;

    JavaVersion(String versionString, int versionNumber) {
        this.versionString = versionString;
        this.versionNumber = versionNumber;
    }
}
