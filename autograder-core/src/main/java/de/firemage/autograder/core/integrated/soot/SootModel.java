package de.firemage.autograder.core.integrated.soot;

import de.firemage.autograder.core.integrated.SpoonModel;
import soot.G;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.JimpleBody;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class SootModel {
    public SootModel(Path jar, SpoonModel spoonModel, Consumer<String> statusConsumer) {
        statusConsumer.accept("Building Jimple");
        // Setup Soot
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(jar.toString());
        Options.v().set_process_dir(List.of(jar.toString()));
        Options.v().set_keep_line_number(true);
        Options.v().set_allow_phantom_refs(false);
        Options.v().set_whole_program(false);
        Options.v().set_verbose(true);

        // Disable unwanted optimizations that could destroy important properties of the code
        Options.v().setPhaseOption("jb.sils", "enabled:false");
        Options.v().setPhaseOption("jb.cp", "enabled:false");

        // We compile with debug info, so we still have the original names available
        // We want to use the to be able to generate readable messages
        Options.v().setPhaseOption("jb", "use-original-names:true");

        SootClass mainClass = Scene.v().loadClassAndSupport(spoonModel.findMain().getDeclaringType().getQualifiedName());
        mainClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod method = mainClass.getMethodByName("foo");
        JimpleBody body = (JimpleBody) method.retrieveActiveBody();
        var analysis = new NullDataflowAnalysis(new ExceptionalUnitGraph(body), spoonModel);
        System.out.println();
        for (Unit unit : body.getUnits()) {
            System.out.println(unit);
            var state = analysis.getFlowAfter(unit);
            for (ValueBox value : unit.getUseAndDefBoxes()) {
                if (value.getValue().getType() instanceof RefLikeType) {
                    System.out.println("\t" + value.getValue() + ": " + state.get(value.getValue()));
                }
            }
        }


        System.out.println();
        for (Unit unit : body.getUnits()) {
            System.out.println(unit.getJavaSourceStartLineNumber() + " (" + unit.getClass().getSimpleName() + "): " + unit);
        }
    }
}
