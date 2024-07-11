package de.firemage.autograder.extra.check.api;

import de.firemage.autograder.core.LocalizedMessage;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.check.ExecutableCheck;
import de.firemage.autograder.extra.errorprone.ErrorProneCheck;
import de.firemage.autograder.extra.errorprone.ErrorProneDiagnostic;
import de.firemage.autograder.extra.errorprone.ErrorProneLint;
import de.firemage.autograder.extra.errorprone.Message;

import java.util.Map;
import java.util.function.Function;

@ExecutableCheck(reportedProblems = {
    ProblemType.COMPARE_TO_ZERO,
    ProblemType.EQUALS_USING_HASHCODE,
    ProblemType.EQUALS_UNSAFE_CAST,
    ProblemType.EQUALS_INCOMPATIBLE_TYPE,
    ProblemType.INCONSISTENT_HASH_CODE,
    ProblemType.UNDEFINED_EQUALS,
    ProblemType.NON_OVERRIDING_EQUALS,
    ProblemType.EQUALS_BROKEN_FOR_NULL,
    ProblemType.ARRAYS_HASHCODE,
    ProblemType.EQUALS_REFERENCE,
    ProblemType.ARRAY_AS_KEY_OF_SET_OR_MAP
})
public class ProblematicEqualsHashCodeComparable implements ErrorProneCheck {
    @Override
    public Map<ErrorProneLint, Function<ErrorProneDiagnostic, Message>> subscribedLints() {
        return Map.ofEntries(
            Map.entry(
                // The result of #compareTo or #compare should only be compared to 0.
                // It is an implementation detail whether a given type returns strictly the values {-1, 0, +1} or others.
                //
                // https://errorprone.info/bugpattern/CompareToZero
                ErrorProneLint.fromString("CompareToZero"),
                diagnostic -> Message.of(
                    ProblemType.COMPARE_TO_ZERO,
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
                    ProblemType.EQUALS_USING_HASHCODE,
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
                    ProblemType.EQUALS_UNSAFE_CAST,
                    new LocalizedMessage("equals-unsafe-cast")
                )
            ),
            Map.entry(
                // An equality test between objects with incompatible types always returns false
                //
                // https://errorprone.info/bugpattern/EqualsIncompatibleType
                ErrorProneLint.fromString("EqualsIncompatibleType"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_INCOMPATIBLE_TYPE,
                    new LocalizedMessage("equals-incompatible-type")
                )
            ),
            Map.entry(
                // Including fields in hashCode which are not compared in equals violates the contract of hashCode.
                //
                // https://errorprone.info/bugpattern/InconsistentHashCode
                ErrorProneLint.fromString("InconsistentHashCode"),
                diagnostic -> Message.of(
                    ProblemType.INCONSISTENT_HASH_CODE,
                    new LocalizedMessage("inconsistent-hashcode")
                )
            ),
            Map.entry(
                // This type is not guaranteed to implement a useful #equals method.
                //
                // https://errorprone.info/bugpattern/UndefinedEquals
                ErrorProneLint.fromString("UndefinedEquals"),
                diagnostic -> Message.of(
                    ProblemType.UNDEFINED_EQUALS,
                    new LocalizedMessage("undefined-equals")
                )
            ),
            Map.entry(
                // equals method doesn't override Object.equals
                //
                // https://errorprone.info/bugpattern/NonOverridingEquals
                ErrorProneLint.fromString("NonOverridingEquals"),
                diagnostic -> Message.of(
                    ProblemType.NON_OVERRIDING_EQUALS,
                    new LocalizedMessage("non-overriding-equals")
                )
            ),
            Map.entry(
                // equals() implementation may throw NullPointerException when given null
                //
                // https://errorprone.info/bugpattern/EqualsBrokenForNull
                ErrorProneLint.fromString("EqualsBrokenForNull"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_BROKEN_FOR_NULL,
                    new LocalizedMessage("equals-broken-for-null")
                )
            ),
            Map.entry(
                // hashcode method on array does not hash array contents
                //
                // https://errorprone.info/bugpattern/ArrayHashCode
                ErrorProneLint.fromString("ArrayHashCode"),
                diagnostic -> Message.of(
                    ProblemType.ARRAYS_HASHCODE,
                    new LocalizedMessage("array-hash-code")
                )
            ),
            Map.entry(
                // == must be used in equals method to check equality to itself or an infinite loop will occur.
                //
                // https://errorprone.info/bugpattern/EqualsReference
                ErrorProneLint.fromString("EqualsReference"),
                diagnostic -> Message.of(
                    ProblemType.EQUALS_REFERENCE,
                    new LocalizedMessage("equals-reference")
                )
            ),
            Map.entry(
                // Arrays do not override equals or hashCode, so comparisons will be done on reference equality
                // only. If neither deduplication nor lookup are needed, consider using a List instead.
                //
                // https://errorprone.info/bugpattern/ArrayAsKeyOfSetOrMap
                ErrorProneLint.fromString("ArrayAsKeyOfSetOrMap"),
                diagnostic -> Message.of(
                    ProblemType.ARRAY_AS_KEY_OF_SET_OR_MAP,
                    new LocalizedMessage("array-as-key-of-set-or-map")
                )
            )
        );
    }
}
