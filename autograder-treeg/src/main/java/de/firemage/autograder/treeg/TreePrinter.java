package de.firemage.autograder.treeg;

public class TreePrinter {
    private final StringBuilder content;
    private int indentLevel;

    public TreePrinter() {
        this.content = new StringBuilder();
        this.indentLevel = 0;
    }

    public void indent() {
        this.indentLevel++;
    }

    public void unindent() {
        this.indentLevel--;
    }

    public void addLine(String content) {
        this.content.append("  ".repeat(this.indentLevel)).append(content).append(System.lineSeparator());
    }

    @Override
    public String toString() {
        return this.content.toString();
    }
}
