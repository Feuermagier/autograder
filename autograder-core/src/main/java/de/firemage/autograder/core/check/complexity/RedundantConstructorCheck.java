package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.api.Translatable;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.function.Predicate;

@ExecutableCheck(reportedProblems = {ProblemType.REDUNDANT_DEFAULT_CONSTRUCTOR})
public class RedundantConstructorCheck extends IntegratedCheck {
    private static final Logger LOG = LoggerFactory.getLogger(RedundantConstructorCheck.class);
    private static final Translatable MESSAGE = new LocalizedMessage("implicit-constructor-exp");

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> element) {
                CtConstructor<?> redundantCtor = null;
                if (element instanceof CtRecord record) {
                    var types = record
                        .getFields()
                        .stream()
                        .map(CtField::getType)
                        .toArray(CtTypeReference<?>[]::new);
                    var canonicalCtor = record.getConstructor(types);

                    if (canonicalCtor != null
                        && !canonicalCtor.isImplicit()
                        && hasEffectivelyDefaultVisibility(record, canonicalCtor)
                        && isDefaultBodyRecord(record, canonicalCtor)) {
                        redundantCtor = canonicalCtor;
                    }
                } else {
                    var ctors = element.getConstructors();
                    if (ctors.size() != 1) return;
                    var ctor = ctors.iterator().next();

                    if (!ctor.isImplicit()
                        && ctor.getParameters().isEmpty()
                        && hasEffectivelyDefaultVisibility(element, ctor)
                        && isDefaultBody(ctor.getBody())
                        && ctor.getThrownTypes().isEmpty()) {
                        redundantCtor = ctor;
                    }
                }
                if (redundantCtor != null) {
                    addLocalProblem(redundantCtor, MESSAGE, ProblemType.REDUNDANT_DEFAULT_CONSTRUCTOR);
                }
            }
        });
    }

    /**
     * Checks if the constructor visibility is effectively the same as the class
     * visibility.
     * <p>
     * A constructor with higher visibility than the containing class is only ever useful
     * with a protected (inner) class, because it can allow the class to be instantiated
     * from a different package.
     */
    private boolean hasEffectivelyDefaultVisibility(CtClass<?> type, CtConstructor<?> ctor) {
        // enum constructors are always private
        if (type.isEnum() || type.isPrivate()) return true;

        if (type.isPublic()) return ctor.isPublic();
        else if (type.isProtected()) return ctor.isProtected();
        // package-access class, only private is smaller
        else return !ctor.isPrivate();
    }

    private boolean isDefaultBody(CtBlock<?> block) {
        return block
            .getStatements()
            .stream()
            .filter(Predicate.not(CtElement::isImplicit))
            // A constructor invocation is either this or super.
            // Because we know we are analyzing the body of a no-args constructor, it
            // cannot be a recursive this() call, but has to be a redundant super() call.
            // If the target is not null it is a qualified super invocation, which is
            // required and not redundant.
            .allMatch(statement -> statement instanceof CtInvocation<?> invocation
                && invocation.getExecutable().isConstructor()
                && invocation.getArguments().isEmpty()
                && invocation.getTarget() == null);
    }

    private boolean isDefaultBodyRecord(CtRecord record, CtConstructor<?> ctor) {
        return ctor
            .getBody()
            .getStatements()
            .stream()
            .filter(Predicate.not(CtElement::isImplicit))
            // The canonical record constructor cannot contain an explicit constructor
            // invocation. We check if all statements are standard field assignments.
            // For a normal constructor this must mean it is the default constructor,
            // because all fields have to be assigned.
            // A compact constructor does not allow field assignments, so this checks
            // for an empty block.
            .allMatch(statement -> {
                if (!(statement instanceof CtAssignment<?, ?> assignment)) return false;
                if (!(assignment.getAssigned() instanceof CtFieldWrite<?> fieldWrite)) return false;
                if (!(assignment.getAssignment() instanceof CtVariableRead<?> variableRead)) return false;
                if (!(variableRead.getVariable() instanceof CtParameterReference<?> parameter)) return false;
                var index = ctor.getParameters().indexOf(parameter.getDeclaration());
                if (index < 0) {
                    LOG.error("encountered CtParameter not present in constructor parameters");
                    return false;
                }
                return record.getFields().get(index) == fieldWrite.getVariable().getDeclaration();
            });
    }
}
