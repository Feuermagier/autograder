package de.firemage.autograder.core.check.oop;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;

import de.firemage.autograder.core.integrated.IdentifierNameUtils;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.SpoonUtil;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.SubtypeFilter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = {
    ProblemType.ABSTRACT_CLASS_WITHOUT_ABSTRACT_METHOD,
    ProblemType.USE_DIFFERENT_VISIBILITY,
    ProblemType.SHOULD_BE_INTERFACE,
    ProblemType.COMPOSITION_OVER_INHERITANCE
})
public class InheritanceBadPractices extends IntegratedCheck {
    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.processWith(new AbstractProcessor<CtClass<?>>() {
            @Override
            public void process(CtClass<?> ctClass) {
                List<CtField<?>> fields = ctClass.getFields();
                Set<CtMethod<?>> methods = ctClass.getMethods();

                List<CtType<?>> subtypes = staticAnalysis.getModel()
                    .getElements(new SubtypeFilter(ctClass.getReference()).includingSelf(false));

                // skip if the class is not inherited from:
                if (subtypes.isEmpty()) {
                    return;
                }

                // check if the class can be an interface:
                if (fields.isEmpty() && ctClass.getSuperclass() == null) {
                    addLocalProblem(
                        ctClass,
                        new LocalizedMessage("should-be-interface"),
                        ProblemType.SHOULD_BE_INTERFACE
                    );
                    // check if the class has only fields (data class)
                } else if (methods.isEmpty() && ctClass.getSuperclass() == null) {
                    String methodName = IdentifierNameUtils.toLowerCamelCase(ctClass.getSimpleName());
                    addLocalProblem(
                        ctClass,
                        new LocalizedMessage(
                            "composition-over-inheritance",
                            Map.of(
                                "suggestion", "%s %s()".formatted(ctClass.getSimpleName(), methodName)
                            )
                        ),
                        ProblemType.COMPOSITION_OVER_INHERITANCE
                    );
                }

                Set<CtConstructor<?>> constructors = ctClass.getConstructors()
                    .stream()
                    .filter(ctElement -> !ctElement.isImplicit())
                    .collect(Collectors.toSet());

                // constructors for abstract classes should not be public
                if (ctClass.isAbstract()) {
                    for (var constructor : constructors) {
                        if (constructor.isPublic()) {
                            addLocalProblem(
                                constructor,
                                new LocalizedMessage(
                                    "use-different-visibility",
                                    Map.of(
                                        "name", ctClass.getSimpleName(),
                                        "suggestion", "protected"
                                    )
                                ),
                                ProblemType.USE_DIFFERENT_VISIBILITY
                            );
                        }
                    }
                }

                // abstract classes must not implement the methods of the interfaces they implement
                // (this can be delegated to the subclasses)
                //
                // in that case it is not a problem, if the abstract class does not have any abstract methods
                List<CtMethod<?>> interfaceMethods = ctClass.getSuperInterfaces()
                    .stream()
                    .map(CtTypeReference::getTypeDeclaration)
                    .filter(Objects::nonNull)
                    .flatMap(ctType -> ctType.getMethods().stream())
                    .filter(ctMethod -> methods.stream().noneMatch(method -> method.isOverriding(ctMethod)))
                    .toList();


                // the abstract class should have at least one abstract method
                if (ctClass.isAbstract()
                    && constructors.stream().noneMatch(CtModifiable::isAbstract)
                    && methods.stream().noneMatch(CtModifiable::isAbstract)
                    && !methods.isEmpty()
                    && interfaceMethods.isEmpty()) {
                    addLocalProblem(
                        ctClass,
                        new LocalizedMessage(
                            "abstract-class-without-abstract-method"
                        ),
                        ProblemType.ABSTRACT_CLASS_WITHOUT_ABSTRACT_METHOD
                    );
                }
            }
        });
    }
}
