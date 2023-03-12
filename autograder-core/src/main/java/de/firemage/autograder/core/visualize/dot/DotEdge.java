package de.firemage.autograder.core.visualize.dot;

import java.util.Map;

public record DotEdge(DotNode start, DotNode end, String cssClass, Map<String, String> styles) {
    public String toDotString() {
        return "\"" + this.start.name() + "\" -> \"" + this.end.name() + "\" " + DotUtil.formatAttributes(this.cssClass, this.styles);
    }
    
    public DotEdge addAttribute(String key, String value) {
        this.styles.put(key, value);
        return this;
    }
}
