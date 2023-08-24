# Statuses
status-compiling = Compiling
status-spotbugs = Running SpotBugs
status-pmd = Running PMD
status-cpd = Running Copy/Paste-Detection
status-error-prone = Running error-prone
status-model = Building the code model
status-docker = Building the Docker image
status-tests = Executing tests
status-integrated = Running integrated analysis

# Linters
linter-cpd = Copy/Paste-Detection
linter-spotbugs = SpotBugs
linter-pmd = PMD
linter-integrated = Integrated Analysis
linter-error-prone = error-prone

merged-problems = {$message} Other problems in {$locations}.

# CPD
duplicate-code = Duplicated code ({$lines}): {$first-path}:{$first-start}-{$first-end} and {$second-path}:{$second-start}-{$second-end}

# API
is-empty-reimplemented-exp = Use isEmpty()

old-collection-exp-vector = Use ArrayList instead of Vector
old-collection-exp-hashtable = Use HashMap instead of Hashtable
old-collection-exp-stack = Use Dequeue instead of Stack

string-is-empty-exp-emptiness = Use 'isEmpty()' instead of '{$exp}' to check for emptiness
string-is-empty-exp-non-emptiness = Use '!<...>isEmpty()' instead of '{$exp}' to check for non-emptiness

use-string-formatted = `{$formatted}` is easier to read.

optional-argument = Optional should not be used as an argument, because it has 3 states: null, Optional.empty() and Optional.of(..). See https://stackoverflow.com/a/31924845/7766117
optional-tri-state = Instead of an Optional boolean, one should use an enum.

equals-hashcode-comparable-contract = Equals and hashCode must always be overridden together. Similarly for Comparable, both equals and hashCode must be overwritten.

use-format-string = `{$formatted}` is easier to read.

use-enum-collection = For maps where an enum is used as a key and for sets as a value, one should use EnumMap/EnumSet.

compare-to-zero = The result of #compareTo or #compare should only be compared to 0.
                It is an implementation detail whether a given type returns strictly the values
                '-1, 0, +1' or others.
equals-using-hashcode = Implementing equals by just comparing hashCodes is fragile.
                Hashes collide frequently, and this will lead to false positives in equals.
equals-unsafe-cast = The contract of equals states that it should return false for incompatible
                    types, while this implementation may throw ClassCastException.
equals-incompatible-type = An equality test between objects with incompatible types always returns false
inconsistent-hashcode = Including fields in hashCode which are not compared in equals violates the contract of hashCode.
undefined-equals = This type is not guaranteed to implement a useful equals method.
non-overriding-equals = equals method doesn't override Object.equals
equals-broken-for-null = equals implementation may throw NullPointerException when given null
array-hash-code = hashCode method on array does not hash array contents
equals-reference = == must be used in equals method to check equality to itself or an infinite loop will occur.
array-as-key-of-set-or-map = Arrays do not override equals or hashCode, so comparisons will be done on reference
                            equality only. If neither deduplication nor lookup are needed, consider using a List
                            instead.

common-reimplementation = The code can be simplified to '{$suggestion}'.

use-entry-set = Use 'entrySet' here instead of 'keySet'.

char-range = The code can be simplified to '{$suggestion}'.

# Comment
commented-out-code-exp = This commented out code should be removed

comment-language-exp-invalid = The language of this comment is neither English nor German but seems to be {$lang}
comment-language-exp-english = The code contains comments in German and in English. This comment is in English. A German comment can be found at {$path}:{$line}
comment-language-exp-german = The code contains comments in German and in English. This comment is in German. An English comment can be found at {$path}:{$line}

javadoc-method-exp-param-missing = The parameter '{$param}' is not mentioned in the JavaDoc comment
javadoc-method-exp-param-unknown = The JavaDoc comment mentions the parameter '{$param}', but the parameter doesn't exist
javadoc-method-exp-unexpected-tag = JavaDoc comments of methods must not have '@{$tag}' tags

javadoc-type-exp-unexpected-tag = JavaDoc comments of types must not have '@{$tag}' tags
javadoc-type-exp-invalid-author = The @author tag should contain your u-shorthand: {$authors}

javadoc-field-exp-unexpected-tag = JavaDoc comments of fields must not have '@{$tag}' tags

javadoc-return-null-exp = The method {$method} may return null but the @return tag doesn't mention it

javadoc-stub-exp-desc = Javadoc has an empty description
javadoc-stub-exp-param = Stub description for parameter {$param}
javadoc-stub-exp-return = Stub description for return value
javadoc-stub-exp-throws = Stub description for exception {$exp}

javadoc-undocumented-throws = The exception {$exp} is thrown, but not mentioned in the javadoc comment.

# Complexity
use-diamond-operator = You can remove the types specified in the `< A, B, ... >` and just use `<>` instead, see https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html and https://stackoverflow.com/a/16352848/7766117

extends-object = Explicitly extending Object is unnecessary

for-loop-var = Each for-loop should have exactly one control variable

implicit-constructor-exp = Unnecessary default constructor

redundant-if-for-bool-exp-return = Directly return {$exp} instead of wrapping it in an if
redundant-if-for-bool-exp-assign = Directly assign {$exp} to {$target} instead of wrapping it in an if

redundant-modifier-desc = Some modifiers are implicit
redundant-modifier-exp = Unnecessary modifier

redundant-return-exp = Unnecessary return

self-assignment-exp = Useless assignment of {$rhs} to {$lhs}

redundant-local-return-exp = Directly return this value

unused-import-exp = Unused import

wrapper-instantiation-exp = Don't instantiate primitive wrappers

repeated-math-operation-mul = Use Math.pow instead of multiplying '{$var}' {count} times with itself.
repeated-math-operation-plus = Use a multiplication with {$count} instead of adding '{$var}' {$count} times to itself.

redundant-neg-exp = '{$original}' should be written as '{$fixed}'

redundant-boolean-equal = It is redundant to explicitly check if a condition equals true or false. Write instead '{$suggestion}'.

use-operator-assignment-exp = Assignment can be simplified to '{$simplified}'

merge-else-if = Use 'else if (...) {"{"} ... {"}"}' instead of 'else {"{"} if (...) {"{"} ... {"}"} {"}"}'

complex-regex = Nontrivial regex need an explanation (score is {$score}, max allowed score is {$max})

redundant-catch = An exception should not be caught and then rethrown immediately.

redundant-array-init = The assignment to the array is unnecessary and can be removed.

redundant-uninitialized-variable = The variable '{$variable}' has been declared, but the value '{$value}' is not directly assigned. Instead you should write '{$suggestion}'.

merge-nested-if = The nested if can be combined with the outer if. The condition for the outer if should then be '{$suggestion}'.

multiple-inline-statements = There should not be multiple statements in a single line. So no declarations of multiple variables or assignments in the same line.

unnecessary-boxing = Instead of the boxed-type one should use '{$suggestion}'.

# Debug
assert-used-exp = Assertions crash the entire program if they evaluate to false.
              Also they can be disabled, so never rely on them to e.g. check user input.
              They are great for testing purposes, but should not be part of your final solution.
              If you want to document an invariant, consider a comment.

print-stack-trace-exp = Don't print stack traces in your final solution

# Exceptions
custom-exception-inheritance-exp-runtime = Custom exceptions should be checked exceptions
custom-exception-inheritance-exp-error = Custom exceptions should not extend Error

empty-catch-block = Empty catch block

exception-controlflow-exp-caught = {$exp} thrown and immediately caught in a surrounding block

runtime-ex-caught-exp = Runtime exception of type {$exp} caught

exception-message = An exception should always have a message that explains what the problem is and ideally how it occurred.

# General

compare-objects-exp = Implement an equals method for type {$type} and use it for comparisons

variable-should-be = The variable '{$variable}' should be '{$suggestion}'.

constants-interfaces-exp = Interfaces must not have fields

param-reassign-exp = Don't reassign method/constructor parameters

double-brace-init = Don't use the obscure 'double brace initialization' syntax

equals-handle-null-argument-exp = equals should handle null arguments

field-local-exp = Field '{$field}' of class {$class} should be converted to a local variable as every method overwrites it before reading it

for-foreach = for-loop should be a for-each-loop

missing-override-exp = Missing @Override

system-dependent-linebreak-exp = Always use system-independent line breaks such as the value obtained from System.lineSeparator() or %n in format strings

field-final-exp = The attribute '{$name}' should be final

string-cmp-exp = Use the equals method: '{$lhs}.equals({$rhs})' instead of '{$lhs} == {$rhs}'

do-not-use-raw-types-exp = Generic Types should always have generics and never be used as raw types, see https://stackoverflow.com/a/2770692/7766117

avoid-labels = Labels should be avoided. See https://stackoverflow.com/a/33689582/7766117.

avoid-shadowing = The variable '{$name}' hides an attribute with the same name. Except for in the constructor, this should be avoided.

suppress-warnings = @SuppressWarnings suppresses warnings from the compiler or Checkstyle, without fixing the underlying problem of the code.

scanner-closed = Scanner should be closed

unchecked-type-cast = It has to be ensured that the type of the object is the same as that of the cast. Otherwise, the code might crash.

compare-char-value = char values in the ASCII range should be compared as char values, not as int values.

use-guard-clauses = The code cancels the normal control-flow through for example a return. if-else-blocks with those conditions can be written more beautifully using so called guard-clauses. This has the advantage that you can better recognize duplicate code. See for a detailed explanation https://medium.com/@scadge/if-statements-design-guard-clauses-might-be-all-you-need-67219a1a981a or https://deviq.com/design-patterns/guard-clause

import-types = Instead of qualifying the type, '{$type}' should be imported. Types from the same package or 'java.lang' do not have to be imported explicitly.

use-different-visibility = The visibility of '{$name}' should be '{$suggestion}'.

avoid-recompiling-regex = The constant is only used with 'Pattern.compile' or 'Pattern.matches'. Convert the constant to a pattern with the value '{$suggestion}'.

# Naming
bool-getter-name = For boolean getters it is recommended to use a verb as a prefix. For example '{$newName}' instead of '{$oldName}'.

constants-name-exp = The name '{$name}' is non-descriptive for the value '{$value}'
constants-name-exp-value = The value '{$value}' of the constant '{$name}' should not be in the name

linguistic-desc = The code element has a confusing name. See https://pmd.github.io/latest/pmd_rules_java_codestyle.html#linguisticnaming

variable-name-single-letter = Single letter names such as '{$name}' are usually non-descriptive
variable-is-abbreviation = Don't use unnecessary abbreviations such as '{$name}'
variable-name-type-in-name = The identifier '{$name}' should not contain its type in the name.
similar-identifier = The identifier '{$left}' is very similar to '{$right}'. This can lead to confusion and typos, which is why it should be renamed.

type-has-descriptive-name-pre-suffix = The name contains redundant prefixes or suffixes
type-has-descriptive-name-exception = A class that inherits from Exception should have 'Exception' at the end of its name

package-naming-convention = The name of a package should be a single word and all letters should be lowercase by convention.
                            Additionally, no special characters should occur like '_'. The following positions do not
                            adhere to this: '{$positions}'

variable-redundant-number-suffix = The identifier '{$name}' has a redundant number at the end.

# OOP
concrete-collection-exp = Use the parent interface instead of a concrete collection class (e.g. List instead of ArrayList)

list-getter-exp = Copy this mutable collection before returning it to avoid unwanted mutations by other classes

method-abstract-exp = {$type}::{$method} should be abstract and not provide a default implementation

utility-exp-final = Utility class is not final
utility-exp-constructor = Utility classes must have a single private no-arg constructor

static-field-exp = The static field '{$name}' must not be static

constants-class-exp = Constants should be saved in the class they are used in and not in a separate class. See https://stackoverflow.com/a/15056462/7766117

interface-static-method-exp = Interfaces should not have static methods, because they can not be overwritten.
interface-static-exp = Interfaces must not be static. The keyword 'static' is redundant and should be removed.

empty-interface-exp = Interfaces should not be empty.

ui-input-separation = Input should not be spread over multiple classes. First use in {$first}.
ui-output-separation = Output should not be spread over multiple classes. First use in {$first}.

do-not-use-system-exit = System.exit() must not be used. Structure your code in so that it exits naturally.

avoid-inner-classes = Every class should be in its own file. Inner-Classes should be avoided.

mutable-enum = Enums should be immutable. See https://stackoverflow.com/a/41199773/7766117

should-be-enum-attribute = The values of the switch should be associated attributes of the enum. Alternatively, one should use a Map.

closed-set-of-values-switch = A switch has only finitely many cases. This is a closed set, which should be modeled as an enum.
closed-set-of-values-list = A list of finitely many values should be modeled as an enum.
closed-set-of-values-method = The method only returns the constant values '{$values}'. There are only finitely many, which is why one should model it as an enum.

do-not-use-instanceof = instanceof should not be used. See Ilias Wiki.
do-not-use-instanceof-emulation = instanceof should not be used and also not be emulated through getClass or ClassCastException. See Ilias Wiki.

abstract-class-without-abstract-method = Abstract classes should have at least one abstract method.
composition-over-inheritance = The parent class has only fields. Instead of inheritance, composition should be used. For example through an interface with the getter: '{$suggestion}'.
should-be-interface = The parent class has only methods without fields. Instead of inheritance an interface with default-implementations should be used.

# Structure

default-package = The default-package should not be used. The following classes are in the default-package: {$positions}

# Unnecessary
empty-block = Empty blocks should be removed or have a comment explaining why they are empty.

unused-element = '{$name}' is unused and should therefore be removed
