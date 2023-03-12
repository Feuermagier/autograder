package de.firemage.autograder.core.visualize.dot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DotGraph {
    private final String name;
    private final HashMap<String, DotNode> nodes;
    private final List<DotEdge> edges;

    public DotGraph(String name) {
        this.name = name;
        this.nodes = new HashMap<>();
        this.edges = new ArrayList<>();
    }

    public DotNode getOrCreateNode(String name) {
        return this.nodes.computeIfAbsent(name, k -> new DotNode(k, null, new HashMap<>()));
    }

    public DotEdge addEdge(DotNode start, DotNode end) {
        var edge = new DotEdge(start, end, null, new HashMap<>());
        this.edges.add(edge);
        return edge;
    }

    public String toDotString() {
        StringBuilder result = new StringBuilder();

        // Header
        result.append("digraph \"%s\" {\n".formatted(this.name));

        // Nodes
        result.append("  {\n    ")
            .append(this.nodes.values().stream().map(DotNode::toDotString).collect(Collectors.joining("\n    ")))
            .append("\n  }");

        // Edges
        result.append("\n  ");
        result.append(this.edges.stream().map(DotEdge::toDotString).collect(Collectors.joining("\n  ")));

        // Last brace
        result.append("\n}");

        return result.toString();
    }

    public String toSvg() throws IOException, InterruptedException {
        String dotString = this.toDotString();
        Process graphviz = new ProcessBuilder("dot", "-Tsvg", "-q").start();
        graphviz.outputWriter().write(dotString);
        graphviz.outputWriter().newLine();
        graphviz.outputWriter().flush();
        graphviz.outputWriter().close();
        
        graphviz.waitFor(5, TimeUnit.SECONDS);
        
        StringBuilder result = new StringBuilder();
        while (graphviz.inputReader().ready()) {
            result.append(graphviz.inputReader().readLine()).append(System.lineSeparator());
        }
        
        graphviz.destroy();
        if (!graphviz.waitFor(2, TimeUnit.SECONDS)) {
            graphviz.destroyForcibly();
            throw new IOException("graphviz timeout");
        }
        
        return result.toString();
    }
}
