package de.firemage.autograder.core.visualize.structure;

import de.firemage.autograder.core.file.UploadedFile;
import de.firemage.autograder.core.integrated.graph.GraphAnalysis;
import de.firemage.autograder.core.integrated.graph.UsageCallMethod;
import de.firemage.autograder.core.integrated.graph.UsageField;
import de.firemage.autograder.core.visualize.dot.DotGraph;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;

import java.io.IOException;

public class StructureVisualizer {
    private final boolean includeJDK;
    private final UploadedFile file;
    private final String basePackage;

    public StructureVisualizer(boolean includeJDK, UploadedFile file) {
        this.includeJDK = includeJDK;
        this.file = file;

        var basePackage = this.file.getModel().getBasePackage();
        if (basePackage.isUnnamedPackage()) {
            this.basePackage = "";
        } else {
            this.basePackage = basePackage.getQualifiedName() + ".";
        }
    }

    public String visualizeAsSvg() throws IOException, InterruptedException {
        DotGraph dot = new DotGraph("Code Structure");
        GraphAnalysis graph = new GraphAnalysis(this.file.getModel());


        graph.getGraph().vertexSet().forEach(vertex -> {
            if (includeTypeReference(vertex)) {
                dot.getOrCreateNode(this.toVertexName(vertex));
            }
        });

        graph.getGraph().edgeSet().forEach(edge -> {
            if (!(edge instanceof UsageCallMethod)) {
                return;
            }
            
            var start = dot.getOrCreateNode(toVertexName(edge.getStart()));
            var end = dot.getOrCreateNode(toVertexName(edge.getEnd()));
            dot.addEdge(start, end);//.addAttribute("label", edge.getClass().getSimpleName());
        });
        return dot.toSvg();
    }

    private boolean includeType(CtType<?> type) {
        return !type.isPrimitive()
            && !type.isShadow()
            && type.getRoleInParent() != CtRole.TYPE_PARAMETER
            && !(!this.includeJDK && type.getQualifiedName().startsWith("java."));
    }

    private boolean includeTypeReference(CtTypeReference<?> type) {
        return !type.isPrimitive()
            && !type.isShadow()
            && type.getTypeDeclaration() != null
            && !(!this.includeJDK && type.getQualifiedName().startsWith("java."));
    }

    private String toVertexName(CtTypeReference<?> type) {
        return type.getQualifiedName().replace(this.basePackage, "");
    }
}
