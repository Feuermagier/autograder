package de.firemage.autograder.core.visualize.dot;

import java.util.Map;
import java.util.Objects;

public record DotNode(String name, String cssClass, Map<String, String> styles) {
    public String toDotString() {
        return "\"" + this.name + "\" " + DotUtil.formatAttributes(this.cssClass, this.styles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DotNode dotNode = (DotNode) o;
        return name.equals(dotNode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
