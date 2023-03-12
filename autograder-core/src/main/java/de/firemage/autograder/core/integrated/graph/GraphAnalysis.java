package de.firemage.autograder.core.integrated.graph;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.DinicMFImpl;
import spoon.reflect.reference.CtTypeReference;

public class GraphAnalysis {
    private final Graph<CtTypeReference<?>, Usage> graph;

    public GraphAnalysis(CodeModel model) {
        this.graph = new GraphBuilder(false).buildGraph(model);
    }
    
    public void partition(CtTypeReference<?> a, CtTypeReference<?> b) {
        var algorithm = new DinicMFImpl<>(this.graph);
        algorithm.calculateMinCut(a, b);
        System.out.println(algorithm.getSinkPartition());
        System.out.println(algorithm.getSourcePartition());
    }
    
    public Graph<CtTypeReference<?>, Usage> getGraph() {
        return this.graph;
    }
}
