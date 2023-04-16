package de.firemage.autograder.core.integrated;

import de.firemage.autograder.core.CodeModel;
import de.firemage.autograder.core.compiler.CompilationResult;
import spoon.processing.Processor;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import java.io.IOException;

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
}
