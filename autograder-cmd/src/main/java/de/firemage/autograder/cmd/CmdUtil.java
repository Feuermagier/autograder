package de.firemage.autograder.cmd;

import picocli.CommandLine;

public final class CmdUtil {
    private static final int CAPTION_SEPARATOR_LENGTH = 100;
    private static final String CAPTION_PADDING = "=";

    private CmdUtil() {

    }

    public static void print(String output) {
        System.out.print(CommandLine.Help.Ansi.AUTO.string(output));
    }

    public static void println(String output) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string(output));
    }

    public static void println() {
        System.out.println();
    }

    public static void printErr(String output) {
        System.err.print(CommandLine.Help.Ansi.AUTO.string(output));
    }

    public static void printlnErr(String output) {
        System.err.println(CommandLine.Help.Ansi.AUTO.string(output));
    }

    public static void printlnErr() {
        System.err.println();
    }

    public static void beginSection(String title) {
        println(StringUtil.center(" " + title + " ", CAPTION_SEPARATOR_LENGTH, CAPTION_PADDING));
        println();
    }

    public static void endSection() {
        println();
        println(CAPTION_PADDING.repeat(CAPTION_SEPARATOR_LENGTH));
        println();
    }
}
