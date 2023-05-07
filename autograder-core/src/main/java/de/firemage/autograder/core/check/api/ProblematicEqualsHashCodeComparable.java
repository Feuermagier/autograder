package de.firemage.autograder.core.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.core.errorprone.ErrorProneCheck;
import de.firemage.autograder.core.errorprone.ErrorProneDiagnostic;
import de.firemage.autograder.core.errorprone.ErrorProneLint;
import de.firemage.autograder.core.errorprone.Message;

import java.util.Map;
import java.util.function.Function;

@ExecutableCheck(reportedProblems = {ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT})
public class ProblematicEqualsHashCodeComparable implements ErrorProneCheck {
    @Override
    public Map<ErrorProneLint, Function<ErrorProneDiagnostic, Message>> subscribedLints() {
        // TODO: add basic tests, problem types and message keys
        return Map.ofEntries(
            Map.entry(
                // The result of #compareTo or #compare should only be compared to 0.
                // It is an implementation detail whether a given type returns strictly the values {-1, 0, +1} or others.
                //
                // https://errorprone.info/bugpattern/CompareToZero
                ErrorProneLint.fromString("CompareToZero"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("compare-to-zero")
                )
            ),
            Map.entry(
                // Implementing #equals by just comparing hashCodes is fragile.
                // Hashes collide frequently, and this will lead to false positives in #equals.
                //
                // https://errorprone.info/bugpattern/EqualsUsingHashCode
                ErrorProneLint.fromString("EqualsUsingHashCode"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("equals-using-hashcode")
                )
            ),
            Map.entry(
                // The contract of #equals states that it should return false for incompatible types,
                // while this implementation may throw ClassCastException.
                //
                // https://errorprone.info/bugpattern/EqualsUnsafeCast
                ErrorProneLint.fromString("EqualsUnsafeCast"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("equals-unsafe-cast")
                )
            ),
            Map.entry(
                // An equality test between objects with incompatible types always returns false
                //
                // https://errorprone.info/bugpattern/EqualsIncompatibleType
                ErrorProneLint.fromString("EqualsIncompatibleType"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("equals-incompatible-type")
                )
            ),
            Map.entry(
                // Including fields in hashCode which are not compared in equals violates the contract of hashCode.
                //
                // https://errorprone.info/bugpattern/InconsistentHashCode
                ErrorProneLint.fromString("InconsistentHashCode"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("inconsistent-hashcode")
                )
            ),
            Map.entry(
                // This type is not guaranteed to implement a useful #equals method.
                //
                // https://errorprone.info/bugpattern/UndefinedEquals
                ErrorProneLint.fromString("UndefinedEquals"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("undefined-equals")
                )
            ),
            Map.entry(
                // equals method doesn't override Object.equals
                //
                // https://errorprone.info/bugpattern/NonOverridingEquals
                ErrorProneLint.fromString("NonOverridingEquals"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("non-overriding-equals")
                )
            ),
            Map.entry(
                // equals() implementation may throw NullPointerException when given null
                //
                // https://errorprone.info/bugpattern/EqualsBrokenForNull
                ErrorProneLint.fromString("EqualsBrokenForNull"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_HASHCODE_COMPARABLE_CONTRACT,
                    new LocalizedMessage("equals-broken-for-null")
                )
            )
        );
    }
}
