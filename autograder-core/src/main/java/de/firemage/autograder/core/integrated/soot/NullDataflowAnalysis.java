package de.firemage.autograder.core.integrated.soot;

import de.firemage.autograder.core.integrated.SpoonModel;
import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AnyNewExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.NullConstant;
import soot.jimple.StaticInvokeExpr;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NullDataflowAnalysis extends ForwardFlowAnalysis<Unit, Map<Value, NullDataflowAnalysis.ReferenceState>> {

    private final Body body;
    private final SpoonModel spoonModel;

    public NullDataflowAnalysis(UnitGraph graph, SpoonModel spoonModel) {
        super(graph);
        this.body = graph.getBody();
        this.spoonModel = spoonModel;
        this.doAnalysis();
    }

    @Override
    protected void flowThrough(Map<Value, NullDataflowAnalysis.ReferenceState> in, Unit d, Map<Value, NullDataflowAnalysis.ReferenceState> out) {
        out.putAll(in);

        for (ValueBox box : d.getUseAndDefBoxes()) {
            Value value = box.getValue();
            if (value instanceof AnyNewExpr) {
                out.put(value, ReferenceState.NOT_NULL);
            } else if (value instanceof InstanceInvokeExpr expr) {
                out.put(value, ReferenceState.TOP);
                // After invoking a method on an object, the object can no longer be null
                out.put(expr.getBase(), ReferenceState.NOT_NULL);
            } else if (value instanceof StaticInvokeExpr expr) {
                if (expr.getMethod().getSubSignature().equals("requireNonNull(java.lang.Object)")) {
                    out.put(value, ReferenceState.NOT_NULL);
                } else {
                    out.put(value, ReferenceState.TOP);
                }
            } else if (value instanceof FieldRef ref) {
                if (ref.getField().isFinal()) {
                    Optional<CtClass<?>> spoonClass = this.spoonModel.findClassByName(ref.getField()
                            .getDeclaringClass()
                            .getJavaStyleName());
                    if (spoonClass.isPresent()) {
                        CtField<?> spoonField = spoonClass.get().getField(ref.getField().getName());
                        if (spoonField.getDefaultExpression() instanceof CtLiteral<?> literal && literal.getValue() == null) {
                            out.put(value, ReferenceState.NULL);
                        } else if (spoonField.getDefaultExpression() != null){
                            out.put(value, ReferenceState.NOT_NULL);
                        } else {
                            out.put(value, ReferenceState.TOP);
                        }
                    } else {
                        // If the class was not found it is a JDK class
                        out.put(value, ReferenceState.TOP);
                    }
                } else {
                    out.put(value, ReferenceState.TOP);
                }
            }
        }

        if (d instanceof DefinitionStmt def) {
            if (def.getLeftOp() instanceof Local local && local.getType() instanceof RefLikeType) {
                ReferenceState state = out.get(def.getRightOp());
                out.put(def.getLeftOp(), state != null ? state : ReferenceState.TOP);
            }
        }
    }

    @Override
    protected Map<Value, NullDataflowAnalysis.ReferenceState> newInitialFlow() {
        Map<Value, NullDataflowAnalysis.ReferenceState> states = new HashMap<>();
        for (Local local : this.body.getLocals()) {
            if (local.getType() instanceof RefLikeType) {
                states.put(local, ReferenceState.BOTTOM);
            }
        }

        for (ValueBox box : this.body.getUseAndDefBoxes()) {
            if (box.getValue() instanceof NullConstant constant) {
                states.put(constant, ReferenceState.NULL);
            }
        }

        return states;
    }

    @Override
    protected void merge(Map<Value, NullDataflowAnalysis.ReferenceState> in1, Map<Value, NullDataflowAnalysis.ReferenceState> in2, Map<Value, NullDataflowAnalysis.ReferenceState> out) {
        for (var entry : in1.entrySet()) {
            Value value = entry.getKey();
            ReferenceState state1 = entry.getValue();
            ReferenceState state2 = in2.get(value);

            if (state1 == state2) {
                out.put(value, state1);
            } else if (state1 == ReferenceState.BOTTOM || state2 == ReferenceState.BOTTOM) {
                out.put(value, ReferenceState.BOTTOM);
            } else {
                out.put(value, ReferenceState.TOP);
            }
        }
    }

    @Override
    protected void copy(Map<Value, NullDataflowAnalysis.ReferenceState> source, Map<Value, NullDataflowAnalysis.ReferenceState> dest) {
        dest.clear();
        dest.putAll(source);
    }

    public enum ReferenceState {TOP, NULL, NOT_NULL, BOTTOM}
}
