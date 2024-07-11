package de.firemage.autograder.core.check.complexity;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.integrated.IntegratedCheck;
import de.firemage.autograder.core.integrated.StaticAnalysis;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtRecord;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.CtScanner;
import spoon.support.reflect.CtExtendedModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ExecutableCheck(reportedProblems = { ProblemType.REDUNDANT_MODIFIER, ProblemType.REDUNDANT_MODIFIER_VISIBILITY_ENUM_CONSTRUCTOR })
public class RedundantModifier extends IntegratedCheck {
    /**
     * The method {@link CtModifiable#getExtendedModifiers()} returns the modifiers in an arbitrary order,
     * because a {@link HashSet} is used internally.
     * To ensure the order is always the same for messages, they are sorted by this method.
     *
     * @param ctModifiable the modifiable to get the modifiers from
     * @return the modifiers in a fixed order
     */
    private static Collection<CtExtendedModifier> getOrderedExtendedModifiers(CtModifiable ctModifiable) {
        List<CtExtendedModifier> result = new ArrayList<>(ctModifiable.getExtendedModifiers());

        result.sort(Comparator.comparing(CtExtendedModifier::getKind));

        return result;
    }

    // TODO: previously the lint marked enum constructors with private as redundant, should we do that too?

    private void reportIfHasModifier(CtModifiable ctModifiable, ModifierKind... modifiers) {
        this.reportIfHasModifier(ctModifiable, ProblemType.REDUNDANT_MODIFIER, modifiers);
    }

    private void reportIfHasModifier(CtModifiable ctModifiable, ProblemType problemType, ModifierKind... modifiers) {
        if (ctModifiable.isImplicit() || !ctModifiable.getPosition().isValidPosition()) {
            return;
        }

        Set<ModifierKind> expectedModifiers = new HashSet<>(Arrays.asList(modifiers));
        List<ModifierKind> presentModifiers = getOrderedExtendedModifiers(ctModifiable)
            .stream()
            .filter(ctExtendedModifier -> !ctExtendedModifier.isImplicit())
            .map(CtExtendedModifier::getKind)
            .filter(expectedModifiers::contains)
            .toList();

        if (presentModifiers.isEmpty()) {
            return;
        }

        this.addLocalProblem(
            ctModifiable,
            new LocalizedMessage(
                "redundant-modifier",
                Map.of(
                    "modifier", presentModifiers.stream()
                        .map(ModifierKind::toString)
                        .collect(Collectors.joining(", "))
                )
            ),
            problemType
        );
    }

    private void checkCtInterface(CtInterface<?> ctInterface) {
        // interfaces are by definition abstract
        this.reportIfHasModifier(ctInterface, ModifierKind.ABSTRACT);

        // all fields in an interface are implicitly public, static and final
        for (CtField<?> ctField : ctInterface.getFields()) {
            this.reportIfHasModifier(ctField, ModifierKind.PUBLIC, ModifierKind.STATIC, ModifierKind.FINAL);
        }

        // all methods in an interface are implicitly public and abstract
        for (CtMethod<?> ctMethod : ctInterface.getMethods()) {
            // NOTE: since Java 9 interfaces can have private methods
            this.reportIfHasModifier(ctMethod, ModifierKind.PUBLIC, ModifierKind.ABSTRACT);
        }

        // all nested types in an interface are implicitly public and static
        for (CtType<?> ctType : ctInterface.getNestedTypes()) {
            this.reportIfHasModifier(ctType, ModifierKind.PUBLIC, ModifierKind.STATIC);
        }
    }

    private void checkCtType(CtType<?> ctType) {
        if (ctType instanceof CtInterface<?> ctInterface) {
            this.checkCtInterface(ctInterface);
        }

        // static modifier is redundant for nested interfaces, enums and records, because they are final by definition
        //
        // nested types in interfaces are covered by the `checkCtInterface`, therefore they are not checked here
        if ((ctType.getDeclaringType() == null || !ctType.getDeclaringType().isInterface())
            && (ctType instanceof CtRecord || ctType.isEnum() || ctType.isInterface())) {
            this.reportIfHasModifier(ctType, ModifierKind.STATIC);
        }
        // static modifier is redundant for nested classes inside enums, because they are implicitly static
        else if (ctType.isClass() && ctType.getDeclaringType() != null && ctType.getDeclaringType().isEnum()) {
            this.reportIfHasModifier(ctType, ModifierKind.STATIC);
        }

        // if a type is final, it can not be extended
        if (ctType.isFinal()) {
            // therefore the final modifier is redundant for methods
            for (CtMethod<?> ctMethod : ctType.getMethods()) {
                this.reportIfHasModifier(ctMethod, ModifierKind.FINAL);
            }
        }

        // a record is implicitly final
        if (ctType instanceof CtRecord ctRecord) {
            this.reportIfHasModifier(ctRecord, ModifierKind.FINAL);
        }

        // a private modifier is redundant for constructors of enums
        if (ctType instanceof CtEnum<?> ctEnum) {
            for (CtConstructor<?> ctConstructor : ctEnum.getConstructors()) {
                this.reportIfHasModifier(ctConstructor, ProblemType.REDUNDANT_MODIFIER_VISIBILITY_ENUM_CONSTRUCTOR, ModifierKind.PRIVATE);
            }
        }
    }

    @Override
    protected void check(StaticAnalysis staticAnalysis) {
        staticAnalysis.getModel().getRootPackage().accept(new CtScanner() {
            @Override
            public <T> void visitCtClass(CtClass<T> ctType) {
                if (ctType.isImplicit() || !ctType.getPosition().isValidPosition()) {
                    super.visitCtClass(ctType);
                    return;
                }

                checkCtType(ctType);

                super.visitCtClass(ctType);
            }

            @Override
            public <T extends Enum<?>> void visitCtEnum(CtEnum<T> ctType) {
                if (ctType.isImplicit() || !ctType.getPosition().isValidPosition()) {
                    super.visitCtEnum(ctType);
                    return;
                }

                checkCtType(ctType);

                super.visitCtEnum(ctType);
            }

            @Override
            public <T> void visitCtInterface(CtInterface<T> ctInterface) {
                if (ctInterface.isImplicit() || !ctInterface.getPosition().isValidPosition()) {
                    super.visitCtInterface(ctInterface);
                    return;
                }

                checkCtType(ctInterface);

                super.visitCtInterface(ctInterface);
            }

            @Override
            public void visitCtRecord(CtRecord ctType) {
                if (ctType.isImplicit() || !ctType.getPosition().isValidPosition()) {
                    super.visitCtRecord(ctType);
                    return;
                }

                checkCtType(ctType);

                super.visitCtRecord(ctType);
            }
        });
    }
}
