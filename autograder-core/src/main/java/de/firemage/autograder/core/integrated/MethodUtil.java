package de.firemage.autograder.core.integrated;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class MethodUtil {
    private MethodUtil() {
    }

    public static boolean isMainMethod(CtMethod<?> method) {
        return method.isStatic()
            && method.isPublic()
            && MethodUtil.isSignatureEqualTo(
            method.getReference(),
            void.class,
            "main",
            java.lang.String[].class
        );
    }

    /**
     * Checks if the given executable reference has the given signature.
     *
     * @param ctExecutableReference the executable reference to check
     * @param returnType the expected return type or null if the return type should not be checked
     * @param methodName the name of the method
     * @param parameterTypes the expected parameter types
     * @return true if the signature matches, false otherwise
     */
    public static boolean isSignatureEqualTo(
        CtExecutableReference<?> ctExecutableReference,
        Class<?> returnType,
        String methodName,
        Class<?>... parameterTypes
    ) {
        TypeFactory factory = ctExecutableReference.getFactory().Type();
        return MethodUtil.isSignatureEqualTo(
            ctExecutableReference,
            returnType == null ? null : factory.createReference(returnType),
            methodName,
            Arrays.stream(parameterTypes).map(factory::createReference).toArray(CtTypeReference[]::new)
        );
    }

    public static boolean isSignatureEqualTo(
        CtExecutableReference<?> ctExecutableReference,
        CtTypeReference<?> returnType,
        String methodName,
        CtTypeReference<?>... parameterTypes
    ) {
        // check that they both return the same type
        if (returnType != null && !TypeUtil.isTypeEqualTo(ctExecutableReference.getType(), returnType)) {
            return false;
        }

        // their names should match:
        if (!ctExecutableReference.getSimpleName().equals(methodName)) {
            return false;
        }

        List<CtTypeReference<?>> givenParameters = ctExecutableReference.getParameters();

        // the number of parameters should match
        if (givenParameters.size() != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            // check if the type of the parameter is equal to the expected type
            if (!TypeUtil.isTypeEqualTo(givenParameters.get(i), parameterTypes[i])) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInOverridingMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return MethodHierarchy.isOverridingMethod(ctMethod);
    }

    public static boolean isOverriddenMethod(CtMethod<?> ctMethod) {
        return MethodHierarchy.isOverriddenMethod(ctMethod);
    }

    /**
     * Checks if the given method is an invocation.
     * @param statement which is checked
     * @return true if the statement is an invocation (instance of CtInvocation, CtConstructorCall or CtLambda),
     * false otherwise
     */
    public static boolean isInvocation(CtStatement statement) {
        return statement instanceof CtInvocation<?> || statement instanceof CtConstructorCall<?> ||
            statement instanceof CtLambda<?>;
    }

    public static boolean isInMainMethod(CtElement ctElement) {
        CtMethod<?> ctMethod = ctElement.getParent(CtMethod.class);
        if (ctMethod == null) {
            return false;
        }

        return MethodUtil.isMainMethod(ctMethod);
    }

    /**
     * Returns the declaration of the given executable reference.
     *
     * @param ctExecutableReference the reference to get the declaration of
     * @return the declaration or null if the declaration could not be found
     */
    public static CtExecutable<?> getExecutableDeclaration(CtExecutableReference<?> ctExecutableReference) {
        return UsesFinder.getExecutableDeclaration(ctExecutableReference);
    }

    public static boolean isGetter(CtMethod<?> method) {
        return method.getSimpleName().startsWith("get")
            && method.getParameters().isEmpty()
            && !method.getType().getSimpleName().equals("void")
            && (method.isAbstract() || StatementUtil.getEffectiveStatements(method.getBody()).size() == 1);
    }

    public static boolean isSetter(CtMethod<?> method) {
        return method.getSimpleName().startsWith("set")
            && method.getParameters().size() == 1
            && method.getType().getSimpleName().equals("void")
            && (method.isAbstract() || StatementUtil.getEffectiveStatements(method.getBody()).size() == 1);
    }

    public static boolean isInSetter(CtElement ctElement) {
        CtMethod<?> parent = ctElement.getParent(CtMethod.class);
        return parent != null && isSetter(parent);
    }

    /**
     * Creates a method from the given statements that can be added to the given target type.
     *
     * @param targetType the type the method should be added to or null if the method should be static
     * @param statements the statements that should be in the method
     * @return a new method that can be added to the target type
     */
    public static UnnamedMethod createMethodFrom(CtType<?> targetType, List<? extends CtStatement> statements) {
        if (targetType == null) {
            targetType = statements.get(0).getParent(CtType.class);
        }
        CtType<?> finalTargetType = targetType;
        Map<CtVariable<?>, List<CtVariableAccess<?>>> args = dependencies(
            statements,
            // filter out all variable accesses that are of the target type (those variables do not have to be passed as arguments)
            ctVariable -> finalTargetType == null
                || !(ctVariable instanceof CtField<?> && ctVariable.getParent(CtType.class) == finalTargetType)
                || !ctVariable.isStatic(),
            ctVariableAccess -> true
        );

        // return variables are those that
        // - are assigned to
        // - are declared in this code segment and used after the last statement -> they have to be returned
        Map<CtVariable<?>, List<CtVariableAccess<?>>> readVariables = new IdentityHashMap<>();
        Map<CtVariable<?>, List<CtVariableAccess<?>>> assignedVariables = new IdentityHashMap<>();

        // now go through all arguments and check if they are assigned to or just read
        args.forEach((ctVariable, ctVariableAccesses) -> {
            boolean isAssigned = ctVariableAccesses.stream().anyMatch(
                ctVariableAccess -> ctVariableAccess instanceof CtVariableWrite<?>
                    && ctVariableAccess.getParent() instanceof CtAssignment<?,?>
            );

            if (isAssigned) {
                assignedVariables.put(ctVariable, ctVariableAccesses);
            } else {
                readVariables.put(ctVariable, ctVariableAccesses);
            }
        });

        return new UnnamedMethod(
            targetType,
            List.copyOf(statements),
            readVariables,
            assignedVariables,
            exposedVariables(statements)
        );
    }

    /**
     * Represents a method that does not have a name yet or a concrete return type.
     * <br>
     * Instead, it separates the accessed variables into those that are assigned to and those that are not.
     *
     * @param parentType the type the method is a part of
     * @param statements the statements of the method
     * @param readVariables the variables that have to be passed as arguments to the method (and lists how they were used)
     * @param assignedVariables variables that are assigned to in the method
     * @param exposedVariables variables that are declared in the method and used after the last statement (those would have to be returned)
     */
    public record UnnamedMethod(
        CtType<?> parentType,
        List<CtStatement> statements,
        Map<CtVariable<?>, List<CtVariableAccess<?>>> readVariables,
        Map<CtVariable<?>, List<CtVariableAccess<?>>> assignedVariables,
        Set<CtVariable<?>> exposedVariables
    ) {
        /**
         * Counts the number of variables (not declared in the code segment) that are assigned in the code segment.
         * @return the number of assigned variables
         */
        private int countAssignedVariables() {
            return this.assignedVariables.size();
        }

        public boolean canBeMethod() {
            // The duplicate code might access variables that are not declared in the code segment.
            // The variables would have to be passed as parameters of a helper method.
            //
            // The problem is that when a variable is reassigned, it can not be passed as a parameter
            // -> we would have to ignore the duplicate code segment
            int numberOfReassignedVariables = this.countAssignedVariables();
            if (numberOfReassignedVariables > 1) {
                return false;
            }

            // Another problem is that the duplicate code segment might declare variables that are used
            // after the code segment.
            //
            // A method can at most return one value (ignoring more complicated solutions like returning a custom object)
            int numberOfUsedVariables = this.exposedVariables().size();

            return numberOfReassignedVariables + numberOfUsedVariables <= 1;
        }
    }

    private static Set<CtVariable<?>> declaredVariables(Collection<? extends CtStatement> statements) {
        return statements.stream()
            .filter(ctStatement -> ctStatement instanceof CtVariable<?>)
            .map(ctStatement -> (CtVariable<?>) ctStatement)
            .collect(Collectors.toCollection(MethodUtil::identitySet));
    }

    /**
     * Finds all variables that are declared in the list of statements and are used after the last statement.
     * @param statements the statements to check
     * @return a set of all variables that are used after the last statement
     */
    private static Set<CtVariable<?>> exposedVariables(List<? extends CtStatement> statements) {
        Set<CtVariable<?>> declaredVariables = declaredVariables(statements);
        Set<CtVariable<?>> result = identitySet();

        if (declaredVariables.isEmpty()) {
            return result;
        }

        // check which declared variables are used after the last statement
        for (CtStatement ctStatement : StatementUtil.getNextStatements(statements.get(statements.size() - 1))) {
            for (CtVariable<?> declaredVariable : declaredVariables) {
                if (UsesFinder.variableUses(declaredVariable).nestedIn(ctStatement).hasAny()) {
                    result.add(declaredVariable);
                }
            }
        }
        return result;
    }

    private static <T> Set<T> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    /**
     * Finds all variables that are used in the given statements and are not declared in them.
     *
     * @param statements the statements to check
     * @param isDependency a predicate that is used to further filter variables. For example, one might want to ignore all class variables.
     * @param isDependencyAccess the predicate can be used to filter out certain variable accesses like assignments or reads.
     * @return a map of all variables that are used in the statements and match the given predicates, where the value is the list of accesses
     */
    private static Map<CtVariable<?>, List<CtVariableAccess<?>>> dependencies(
        Collection<? extends CtStatement> statements,
        Predicate<? super CtVariable<?>> isDependency,
        Predicate<? super CtVariableAccess<?>> isDependencyAccess
    ) {
        if (statements.isEmpty()) {
            return new IdentityHashMap<>();
        }

        // all variables declared in the code segment (including nested ones)
        Set<CtVariable<?>> codeSegmentVariables = statements.stream()
            .flatMap(ctStatement -> ctStatement.getElements(new TypeFilter<CtVariable<?>>(CtVariable.class)).stream())
            .collect(Collectors.toCollection(MethodUtil::identitySet));

        return statements.stream()
            .flatMap(ctStatement -> ctStatement.getElements(new TypeFilter<CtVariableAccess<?>>(CtVariableAccess.class)).stream())
            .filter(isDependencyAccess)
            .filter(ctVariableAccess -> UsesFinder.getDeclaredVariable(ctVariableAccess) != null)
            .map(ctVariableAccess -> Map.entry(UsesFinder.getDeclaredVariable(ctVariableAccess), ctVariableAccess))
            .filter(entry -> !codeSegmentVariables.contains(entry.getKey()) && isDependency.test(entry.getKey()))
            .collect(Collectors.groupingBy(Map.Entry::getKey, IdentityHashMap::new, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }


    public static boolean hasBeenInvoked(CtExecutable<?> ctExecutable) {
        // NOTE: at the moment, overrides are not considered uses -> every other use would be an invocation
        return UsesFinder.getAllUses(ctExecutable).hasAny();
    }
}
