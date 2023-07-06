package de.firemage.autograder.span;

public enum Style {
    ERROR(Color.RED, '^', '^'),
    WARNING(Color.YELLOW, '^', '^'),
    NOTE(Color.BLUE, '-', '-'),
    HELP(Color.GREEN, '-', '-');

    private final Color color;
    private final char underline;
    private final char marker;

    private Style(Color color, char underline, char marker) {
        this.color = color;
        this.underline = underline;
        this.marker = marker;
    }

    public String marker() {
        return String.valueOf(this.marker);
    }
}
