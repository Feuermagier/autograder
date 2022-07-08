package de.firemage.autograder.core.integrated.soot;

import de.firemage.autograder.core.file.UploadedFile;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.JimpleBody;
import soot.options.Options;

public class SootAnalysis {
    public SootAnalysis(UploadedFile file, String main) {
        // Setup Soot
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_soot_classpath(file.getFile().toString());
        SootClass mainClass = Scene.v().loadClassAndSupport(main);
        mainClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod method = mainClass.getMethodByName("foo");
        JimpleBody body = (JimpleBody) method.getActiveBody();
        System.out.println(method.getSignature());
        for (Unit unit : body.getUnits()) {
            System.out.println(unit);
        }
    }
}
