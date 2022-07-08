package de.firemage.autograder.core.integrated.graph;

import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class GraphBuilder {
    private final boolean includeJDK;

    public GraphBuilder(boolean includeJDK) {
        this.includeJDK = includeJDK;
    }

    public Graph<CtTypeReference<?>, Usage> buildGraph(StaticAnalysis analysis) {
        Graph<CtTypeReference<?>, Usage> graph = new DirectedMultigraph<>(Usage.class);

        analysis.getSpoonModel().processWith(new AbstractProcessor<CtType<?>>() {
            @Override
            public void process(CtType<?> type) {
                createVertex(type.getReference(), graph);

                for (CtField<?> field : type.getFields()) {
                    addField(type.getReference(), field, graph);
                }

                type.filterChildren(CtInvocation.class::isInstance).forEach((CtInvocation<?> i) -> {
                    var executable = i.getExecutable().getExecutableDeclaration();
                    if (executable instanceof CtMethod<?> method) {
                        addMethodCall(type.getReference(), method, graph);
                    } else if (executable instanceof CtConstructor<?> constructor) {
                        addInstanceCreation(type.getReference(), constructor, graph);
                    } else {
                        throw new IllegalStateException(executable.getClass().getSimpleName());
                    }
                });

                type.filterChildren(CtConstructorCall.class::isInstance).forEach((CtConstructorCall<?> c) -> {
                    var executable = c.getExecutable().getExecutableDeclaration();
                    if (executable instanceof CtConstructor<?> constructor) {
                        addInstanceCreation(type.getReference(), constructor, graph);
                    } else {
                        throw new IllegalStateException(executable.getClass().getSimpleName());
                    }
                });

                type.filterChildren(CtFieldAccess.class::isInstance).forEach((CtFieldAccess<?> a) -> {
                    var field = a.getVariable().getFieldDeclaration();
                    if (field == null) {
                        // e.g. for array.length
                        return;
                    }
                    addFieldAccess(type.getReference(), field, graph);
                });
            }
        });
        //var sets = new ConnectivityInspector<>(graph).connectedSets();
        return graph;
    }

    private void createVertex(CtTypeReference<?> type, Graph<CtTypeReference<?>, Usage> graph) {
        if (includeType(type) && !graph.containsVertex(type)) {
            graph.addVertex(type);
        }
    }

    private void addMethodCall(CtTypeReference<?> start, CtMethod<?> method,
                                      Graph<CtTypeReference<?>, Usage> graph) {
        CtTypeReference<?> target = method.getDeclaringType().getReference();
        if (includeType(start) && includeType(target) && !start.equals(target)) {
            createVertex(target, graph);
            graph.addEdge(start, target, new UsageCallMethod(start, target, method));
        }
    }

    private void addField(CtTypeReference<?> start, CtField<?> field,
                                       Graph<CtTypeReference<?>, Usage> graph) {
        addReferenceViaField(start, field, field.getType(), 0, graph);
    }
    
    private void addReferenceViaField(CtTypeReference<?> start, CtField<?> field, CtTypeReference<?> type, int index,
                                             Graph<CtTypeReference<?>, Usage> graph) {
        if (includeType(start) && includeType(type) && !start.equals(type)) {
            createVertex(type, graph);
            graph.addEdge(start, type, new UsageField(start, type, field, index));
        }
        for (CtTypeReference<?> parameter : type.getActualTypeArguments()) {
            addReferenceViaField(start, field, parameter, index + 1, graph);
        }
    }

    private void addInstanceCreation(CtTypeReference<?> start, CtConstructor<?> constructor,
                                            Graph<CtTypeReference<?>, Usage> graph) {
        CtTypeReference<?> target = constructor.getDeclaringType().getReference();
        if (includeType(start) && includeType(target) && !start.equals(target)) {
            createVertex(target, graph);
            graph.addEdge(start, target, new UsageCreateInstance(start, target, constructor));
        }
    }

    private void addFieldAccess(CtTypeReference<?> start, CtField<?> field,
                                       Graph<CtTypeReference<?>, Usage> graph) {
        CtTypeReference<?> target = field.getDeclaringType().getReference();
        if (includeType(start) && includeType(target) && !start.equals(target)) {
            createVertex(target, graph);
            graph.addEdge(start, target, new UsageAccessField(start, target, field));
        }
    }

    private boolean includeType(CtTypeReference<?> type) {
        return !type.isPrimitive()
            && !type.isShadow()
            && type.getTypeDeclaration() != null
            && !(!this.includeJDK && type.getQualifiedName().startsWith("java."));
    }

   public void exportToFile(Graph<CtTypeReference<?>, Usage> graph, String filename) {
        try {
            var exporter = new DOTExporter<CtTypeReference<?>, Usage>();
            exporter.setVertexAttributeProvider(
                type -> Map.of("label", new DefaultAttribute<>(type.getQualifiedName(), AttributeType.STRING)));
            exporter.setEdgeAttributeProvider(usage -> {
                if (usage instanceof UsageField field) {
                    return Map.of("label",
                        new DefaultAttribute<>(field.getField().getSimpleName() + "#" + field.getTypeParameterIndex(), AttributeType.STRING));
                } else if (usage instanceof UsageCallMethod method) {
                    return Map.of("label",
                        new DefaultAttribute<>(method.getMethod().getSignature(), AttributeType.STRING));
                } else if (usage instanceof UsageCreateInstance constructor) {
                    String signature = constructor.getConstructor().getSignature();
                    return Map.of("label",
                        new DefaultAttribute<>("new " + signature.substring(
                            signature.indexOf(constructor.getConstructor().getDeclaringType().getSimpleName())),
                            AttributeType.STRING));
                } else if (usage instanceof UsageAccessField field) {
                    return Map.of("label",
                        new DefaultAttribute<>("->" + field.getField().getSimpleName(), AttributeType.STRING));
                } else {
                    throw new IllegalStateException();
                }
            });
            var writer = new FileWriter(filename);
            exporter.exportGraph(graph, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
