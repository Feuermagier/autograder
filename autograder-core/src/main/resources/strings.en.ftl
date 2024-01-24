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

suggest-replacement = Use '{$suggestion}' instead of '{$original}'.
common-reimplementation = The code can be simplified to '{$suggestion}'.

use-string-formatted = '{$formatted}' is easier to read.
use-format-string = '{$formatted}' is easier to read.

optional-argument = Optional should not be used as an argument, because it has 3 states: null, Optional.empty() and Optional.of(..). See https://stackoverflow.com/a/31924845/7766117
optional-tri-state = Instead of an Optional boolean, one should use an enum.

equals-hashcode-comparable-contract = Equals and hashCode must always be overridden together. Similarly for Comparable, both equals and hashCode must be overwritten.

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

implement-comparable = The type '{$name}' should implement `Comparable<{$name}>`, then the `Comparator` becomes redundant.

# Comment
commented-out-code = This commented out code should be removed

comment-language-exp-invalid = The language of this comment is neither English nor German but seems to be {$lang}
comment-language-exp-english = The code contains comments in German and in English. This comment is in English. A German comment can be found at {$path}:{$line}
comment-language-exp-german = The code contains comments in German and in English. This comment is in German. An English comment can be found at {$path}:{$line}

unnecessary-comment-empty = This comment is empty and should therefore be removed

javadoc-method-exp-param-missing = The parameter '{$param}' is not mentioned in the JavaDoc comment
javadoc-method-exp-param-unknown = The JavaDoc comment mentions the parameter '{$param}', but the parameter doesn't exist

javadoc-unexpected-tag = The JavaDoc comment should not have an '@{$tag}' tag.

javadoc-type-exp-invalid-author = The @author tag should contain your u-shorthand: {$authors}

javadoc-return-null-exp = The method {$method} may return null but the @return tag doesn't mention it

javadoc-stub-exp-desc = Javadoc has an empty description
javadoc-stub-exp-param = Stub description for parameter {$param}
javadoc-stub-exp-return = Stub description for return value
javadoc-stub-exp-throws = Stub description for exception {$exp}

javadoc-undocumented-throws = The exception {$exp} is thrown, but not mentioned in the javadoc comment.

todo-comment = TODOs should not be in the final submission.

# Complexity
use-diamond-operator = You can remove the types specified in the '< A, B, ... >' and just use '<>' instead, see https://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html and https://stackoverflow.com/a/16352848/7766117

extends-object = Explicitly extending Object is unnecessary

for-loop-var = Each for-loop should have exactly one control variable

implicit-constructor-exp = Unnecessary default constructor

redundant-modifier = The following modifiers are redundant and should be removed: {$modifier}.

redundant-return-exp = Unnecessary return

self-assignment-exp = Useless assignment of {$rhs} to {$lhs}

too-many-exceptions = The project defines {$count} exceptions. Those are too many.

redundant-variable = The variable '{$name}' is redundant, the value can be used directly: '{$suggestion}'.

unused-import = The import '{$import}' is unused and should therefore be removed.

redundant-boolean-equal = It is redundant to explicitly check if a condition equals true or false. Write instead '{$suggestion}'.

use-operator-assignment-exp = Assignment can be simplified to '{$simplified}'

complex-regex = Nontrivial regex need an explanation (score is {$score}, max allowed score is {$max})

redundant-catch = An exception should not be caught and then rethrown immediately.

redundant-uninitialized-variable = The variable '{$variable}' has been declared, but the value '{$value}' is not directly assigned. Instead you should write '{$suggestion}'.

merge-nested-if = The nested if can be combined with the outer if. The condition for the outer if should then be '{$suggestion}'.

multiple-inline-statements = There should not be multiple statements in a single line. So no declarations of multiple variables or assignments in the same line.

multi-threading = Multithreading is out of scope for this lecture. The code is only executed on a single thread. Writing it thread-safe makes the code unnecessarily complex.

try-catch-complexity = The complexity of try-catch-blocks should be kept low. There should be less than {$max} statements in the try-block. You should refactor the code into multiple methods or extract unnecessary statements out of the try-block.

# Debug
assert-used-exp = Assertions crash the entire program if they evaluate to false.
              Also they can be disabled, so never rely on them to e.g. check user input.
              They are great for testing purposes, but should not be part of your final solution.
              If you want to document an invariant, consider a comment.

print-stack-trace = Don't print stack traces in your final solution

# Exceptions
custom-exception-inheritance-error = The custom exception '{$name}' should not extend 'Error'.
custom-exception-inheritance-runtime = The custom exception '{$name}' should extend 'Exception' and not 'RuntimeException'.

empty-catch-block = Empty catch block

exception-controlflow-caught = {$exception} thrown and immediately caught in a surrounding block
exception-controlflow-should-not-be-caught = {$exception} should never be caught

runtime-exception-caught = RuntimeExceptions '{$exception}' should not be caught

exception-message = An exception should always have a message that explains what the problem is and ideally how it occurred.

# General

compare-objects-exp = Implement an equals method for type {$type} and use it for comparisons

variable-should-be = The variable '{$variable}' should be '{$suggestion}'.

constants-interfaces-exp = Interfaces must not have fields

reassigned-parameter = The parameter '{$name}' should not be assigned a new value.

double-brace-init = Don't use the obscure 'double brace initialization' syntax

field-local-exp = Field '{$field}' of class {$class} should be converted to a local variable as every method overwrites it before reading it

missing-override = '{$name}' should have an '@Override'-annotation, see https://stackoverflow.com/a/94411/7766117.

system-specific-linebreak = Always use system-independent line breaks such as the value obtained from System.lineSeparator() or %n in format strings

field-final-exp = The attribute '{$name}' should be final

string-cmp-exp = Use the equals method: '{$lhs}.equals({$rhs})' instead of '{$lhs} == {$rhs}'

do-not-use-raw-types-exp = Generic Types should always have generics and never be used as raw types, see https://stackoverflow.com/a/2770692/7766117

avoid-labels = Labels should be avoided. See https://stackoverflow.com/a/33689582/7766117.

avoid-shadowing = The variable '{$name}' hides an attribute with the same name. Except for in the constructor, this should be avoided.

suppress-warnings = @SuppressWarnings suppresses warnings from the compiler or Checkstyle, without fixing the underlying problem of the code.

scanner-closed = Scanner should be closed

unchecked-type-cast = It has to be ensured that the type of the object is the same as that of the cast. Otherwise, the code might crash.

compare-char-value = Here '{$expression}' of type char is compared with the value {$intValue}. It is not obvious which letter the value represents, therefore write '{$charValue}'.

use-guard-clauses = The code cancels the normal control-flow through for example a return. if-else-blocks with those conditions can be written more beautifully using so called guard-clauses. This has the advantage that you can better recognize duplicate code. See for a detailed explanation https://medium.com/@scadge/if-statements-design-guard-clauses-might-be-all-you-need-67219a1a981a or https://deviq.com/design-patterns/guard-clause

import-types = Instead of qualifying the type, '{$type}' should be imported. Types from the same package or 'java.lang' do not have to be imported explicitly.

use-different-visibility = The visibility of '{$name}' should be '{$suggestion}'.

avoid-recompiling-regex = The constant is only used with 'Pattern.compile' or 'Pattern.matches'. Convert the constant to a pattern with the value '{$suggestion}'.

binary-operator-on-boolean = Instead of '|' and '&' one should use '||' and '&&'.

object-datatype = Instead of the datatype 'Object', the variable '{$variable}' should have a concrete or generic datatype.

magic-string = The string '{$value}' should be in a constant. See the wiki article for magic strings.


# Naming
bool-getter-name = For boolean getters it is recommended to use a verb as a prefix. For example '{$newName}' instead of '{$oldName}'.

constants-name-exp = The name '{$name}' is non-descriptive for the value '{$value}'
constants-name-exp-value = The value '{$value}' of the constant '{$name}' should not be in the name

linguistic-naming-boolean = The name of '{$name}' indicates that it is/returns a boolean instead of '{$type}'.
linguistic-naming-getter = The name of '{$name}' indicates that it returns a value, which is not the case.
linguistic-naming-setter = The name of '{$name}' indicates that it is a setter, which should not return a value.

variable-name-single-letter = Single letter names such as '{$name}' are usually non-descriptive
variable-is-abbreviation = Don't use unnecessary abbreviations such as '{$name}'
variable-name-type-in-name = The identifier '{$name}' should not contain its type in the name.
similar-identifier = The identifier '{$left}' is very similar to '{$right}'. This can lead to confusion and typos, which is why it should be renamed.

type-has-descriptive-name-pre-suffix = The name contains redundant prefixes or suffixes
type-has-descriptive-name-exception = A class that inherits from Exception should have 'Exception' at the end of its name

package-naming-convention = The name of a package should be a single word and all letters should be lowercase by convention.
                            Except for the character '_', no special characters should appear. The following positions do not
                            adhere to this: '{$positions}'

variable-redundant-number-suffix = The identifier '{$name}' has a redundant number at the end.

# OOP
concrete-collection = The type '{$type}' should be replaced by an interface like 'List' or 'Set'.

list-getter-exp = Copy this mutable collection before returning it to avoid unwanted mutations by other classes

method-abstract-exp = {$type}::{$method} should be abstract and not provide a default implementation

utility-exp-final = Utility class is not final
utility-exp-constructor = Utility classes must have a single private no-arg constructor

static-field-should-be-instance = The static field '{$name}' must not be static.

constants-class-exp = Constants should be saved in the class they are used in and not in a separate class. See https://stackoverflow.com/a/15056462/7766117

interface-static-method-exp = Interfaces should not have static methods, because they can not be overwritten.

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

avoid-static-blocks = Static blocks should be avoided because they are not expandable and object-oriented. Static blocks should be replaced by an object-oriented solution (e.g. constructor).

# Structure

default-package = The default-package should not be used. The following classes are in the default-package: {$positions}
too-few-packages = The project has more than {$max} classes, but only one package is used. You should think about a better classification for the different classes to distribute the classes over multiple packages (e.g. commands, logic, ui, ...).

# Unnecessary
empty-block = Empty blocks should be removed or have a comment explaining why they are empty.

unused-element = '{$name}' is unused and should therefore be removed
