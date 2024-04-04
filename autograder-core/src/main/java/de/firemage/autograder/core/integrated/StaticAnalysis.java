package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.compiler.CompilationResult;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtImport;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.concurrent.atomic.AtomicBoolean;

public class StaticAnalysis {
    private final CodeModel model;
    private final CompilationResult compilationResult;

    public StaticAnalysis(CodeModel model, CompilationResult compilationResult) {
        this.model = model;
        this.compilationResult = compilationResult;
    }

    public Factory getFactory() {
        return this.model.getFactory();
    }

    public CtModel getModel() {
        return this.model.getModel();
    }

    public CodeModel getCodeModel() {
        return this.model;
    }

    public CompilationResult getCompilationResult() {
        return this.compilationResult;
    }

    public <E extends CtElement> void processWith(Processor<E> processor) {
        this.model.processWith(processor);
    }

    private static boolean isJavaUtilImport(CtImport ctImport) {
        CtPackageReference ctPackageReference = null;
        if (ctImport.getReference() instanceof CtTypeReference<?> ctTypeReference) {
            ctPackageReference = ctTypeReference.getTypeDeclaration().getPackage().getReference();
        } else if (ctImport.getReference() instanceof CtPackageReference packageReference) {
            ctPackageReference = packageReference;
        }

        return ctPackageReference != null && ctPackageReference.getQualifiedName().equals("java.util");
    }

    public boolean hasJavaUtilImport() {
        AtomicBoolean hasImport = new AtomicBoolean(false);
        SpoonUtil.visitCtCompilationUnit(this.getModel(), ctCompilationUnit -> {
            if (hasImport.get()) {
                return;
            }

            for (CtImport ctImport : ctCompilationUnit.getImports()) {
                if (isJavaUtilImport(ctImport)) {
                    hasImport.set(true);
                    break;
                }
            }
        });

        return hasImport.get();
    }
}
