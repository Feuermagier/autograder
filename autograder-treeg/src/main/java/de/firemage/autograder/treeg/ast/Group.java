package de.firemage.autograder.treeg.ast;

import de.firemage.autograder.treeg.TreePrinter;

public record Group(RegExNode root, String name, Type type, String flags) implements RegExNode {
    @Override
    public String toRegEx() {
        String prefix = switch (this.type) {
            case CAPTURING -> this.name != null ? "?<" + this.name + ">" : "";
            case NON_CAPTURING -> "?" + this.flags + ":";
            case INDEPENDENT_NON_CAPTURING -> "?>";
        };
        return "("  + prefix + this.root.toRegEx() + ")";
    }

    @Override
    public void toTree(TreePrinter printer) {
        if (this.name != null) {
            printer.addLine("Named Group ('" + this.name + "')");
        } else {
            printer.addLine("Group");
        }
        printer.indent();
        this.root.toTree(printer);
        printer.unindent();
    }

    public enum Type {
        CAPTURING,
        NON_CAPTURING,
        INDEPENDENT_NON_CAPTURING
    }
}
