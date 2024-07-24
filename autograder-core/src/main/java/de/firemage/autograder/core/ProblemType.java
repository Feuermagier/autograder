package de.firemage.autograder.core;

import de.firemage.autograder.api.AbstractProblemType;
import de.firemage.autograder.api.HasFalsePositives;

public enum ProblemType implements AbstractProblemType {
    /**
     * If the code is split into multiple packages, all input must happen in one package. Otherwise, one class must do all input.
     * <br/>
     * Has false positives.
     */
    @HasFalsePositives
    UI_INPUT_SEPARATION,

    /**
     * If the code is split into multiple packages, all output must happen in one package. Otherwise, one class must do all output.
     * <br/>
     * Has false positives.
     */
    @HasFalsePositives
    UI_OUTPUT_SEPARATION,

    /**
     * Checks for non-private inner types (static classes, interfaces, enums, local types)
     * <br>
     * This check ignores inner classes that are not static, because there are cases where
     * that can be useful.
     */
    AVOID_INNER_CLASSES,

    /**
     * Advises to use String.formatted instead of String.format for simple format strings
     */
    @HasFalsePositives
    USE_STRING_FORMATTED,

    /**
     * Reports all java.util.Optional<Boolean> types
     */
    OPTIONAL_TRI_STATE,

    /**
     * Reports all uses of labels.
     * <br>
     * For example `label: while (true) { ... }`
     */
    AVOID_LABELS,

    /**
     * Advises to use Arrays.fill(array, val) instead of Arrays.fill(array, val, 0, array.length)
     */
    SIMPLIFY_ARRAYS_FILL,

    /**
     * Reports unused assignments
     */
    @HasFalsePositives
    REDUNDANT_ASSIGNMENT,

    /**
     * Reports local variables & fields that shadow files in parent classes, skipping constructors and simple setters
     */
    @HasFalsePositives
    AVOID_SHADOWING,


    /**
     * Suggests to use the `Collections.nCopies` method when applicable.
     */
    @HasFalsePositives
    COLLECTIONS_N_COPIES,

    /**
     * Reports all calls to `System.exit`
     */
    @HasFalsePositives
    DO_NOT_USE_SYSTEM_EXIT,

    /**
     * Checks if a `Scanner` is closed anywhere in the project or closed through a try-with.
     */
    @HasFalsePositives
    SCANNER_MUST_BE_CLOSED,

    /**
     * Reports when the equals/hashCode/Comparable contract is violated by not implementing equals or hashCode.
     */
    @HasFalsePositives
    EQUALS_HASHCODE_COMPARABLE_CONTRACT,

    /**
     * Reports when a type cast is performed without checking the type.
     * <br>
     * This uses the java compiler to find unchecked type casts. Therefore it should not have any false-positives/negatives.
     */
    UNCHECKED_TYPE_CAST,

    /**
     * Reports when there are more than some number (or percentage) of exceptions in a project.
     */
    @HasFalsePositives
    TOO_MANY_EXCEPTIONS,

    /**
     * Suggests implementing comparable instead of using a comparator.
     */
    @HasFalsePositives
    IMPLEMENT_COMPARABLE,

    /**
     * Reports magic strings.
     */
    @HasFalsePositives
    MAGIC_STRING,

    /**
     * Checks if a constant has its value in its name. For example `public static final int TEN = 10;`.
     * <br>
     * One problem with this check is that it can never be reliable. For example
     * `public static final String QUIT_COMMAND_NAME = "quit";` contains the value, but that one is fine.
     * This is currently solved by ignoring all constants where more than X% of the name is not the value.
     */
    @HasFalsePositives
    CONSTANT_NAME_CONTAINS_VALUE,

    /**
     * Reports all uses of deprecated collections like Vector, Hashtable, Stack, etc.
     */
    @HasFalsePositives
    DEPRECATED_COLLECTION_USED,

    /**
     * When someone does for example list.size() == 0 instead of list.isEmpty().
     */
    @HasFalsePositives
    COLLECTION_IS_EMPTY_REIMPLEMENTED,

    /**
     * When someone does for example "".equals(str) instead of str.isEmpty().
     */
    @HasFalsePositives
    STRING_IS_EMPTY_REIMPLEMENTED,

    /**
     * Checks that the @author tag is valid. (should be u-shorthand)
     */
    @HasFalsePositives
    INVALID_AUTHOR_TAG,

    /**
     * Heuristic to find commented out code.
     * <br>
     * This check is neither able to find all occurrences of commented out code nor is it able to avoid false positives.
     * The current implementation works quite well.
     */
    @HasFalsePositives
    COMMENTED_OUT_CODE,

    /**
     * Uses AI to find comments that are not in the same language as all other comments.
     * <br>
     * Obviously has false positives.
     */
    @HasFalsePositives
    INCONSISTENT_COMMENT_LANGUAGE,

    /**
     * Tries to detect if an enum is mutable, which would be global state.
     * <br>
     * Detecting mutability reliably is impossible and we do not want to have any false positives.
     * Therefore this check is very conservative and will only report very obvious cases.
     */
    @HasFalsePositives
    MUTABLE_ENUM,

    /**
     * Reports code where methods Character.isDigit are reimplemented (e.g. `c >= '0' && c <= '9'`).
     */
    @HasFalsePositives
    CHAR_RANGE,

    /**
     * Similar to {@link ProblemType#INCONSISTENT_COMMENT_LANGUAGE}, but reports comments where the AI thinks that the comment is neither german nor english.
     * <br>
     * Not very reliable and does not detect the obvious cases like japanese characters.
     */
    @HasFalsePositives
    INVALID_COMMENT_LANGUAGE,

    /**
     * Reports javadoc where the description is left empty.
     */
    @HasFalsePositives
    JAVADOC_STUB_DESCRIPTION,

    /**
     * Reports javadoc where the param tag is left empty.
     */
    @HasFalsePositives
    JAVADOC_STUB_PARAMETER_TAG,

    /**
     * Reports javadoc where the return tag is left empty.
     */
    @HasFalsePositives
    JAVADOC_STUB_RETURN_TAG,

    /**
     * Reports javadoc where the throws tag is left empty.
     */
    @HasFalsePositives
    JAVADOC_STUB_THROWS_TAG,

    /**
     * Reports javadoc where a param tag is missing.
     */
    @HasFalsePositives
    JAVADOC_MISSING_PARAMETER_TAG,

    /**
     * Reports javadoc where a param tag is documented, but there is no param.
     */
    @HasFalsePositives
    JAVADOC_UNKNOWN_PARAMETER_TAG,

    /**
     * Reports violations of the package naming convention.
     */
    @HasFalsePositives
    PACKAGE_NAMING_CONVENTION,

    /**
     * Reports methods where an exception is thrown, but not documented through a throws tag.
     */
    @HasFalsePositives
    JAVADOC_UNDOCUMENTED_THROWS,

    /**
     * Reports javadoc tags that are not expected in that doc. Like an @author in a method.
     */
    @HasFalsePositives
    JAVADOC_UNEXPECTED_TAG,

    /**
     * Reports code where generics are explicitly typed instead of letting the java compiler do its job; new ArrayList<String>() instead of new ArrayList<>().
     * <br>
     * This check uses a very complicated implementation.
     */
    @HasFalsePositives
    UNUSED_DIAMOND_OPERATOR,

    /**
     * Reports classes that explicitly extend Object.
     */
    @HasFalsePositives
    EXPLICITLY_EXTENDS_OBJECT,

    /**
     * Reports for loops with multiple variables.
     */
    @HasFalsePositives
    FOR_WITH_MULTIPLE_VARIABLES,

    /**
     * Reports code where binary operators are used on booleans instead of logical operators.
     */
    @HasFalsePositives
    BINARY_OPERATOR_ON_BOOLEAN,

    /**
     * Reports TODO comments.
     */
    @HasFalsePositives
    TODO_COMMENT,

    /**
     * Reports code where the default constructor of a type is explicitly defined.
     */
    @HasFalsePositives
    REDUNDANT_DEFAULT_CONSTRUCTOR,

    /**
     * Reports code like if (a) { return true; } else { return false; } that could be replaced with return a;
     */
    @HasFalsePositives
    REDUNDANT_IF_FOR_BOOLEAN,

    /**
     * Reports redundant modifiers like an interface in a class having a static modifier.
     */
    @HasFalsePositives
    REDUNDANT_MODIFIER,

    /**
     * Reports enum constructor with a visibility modifier. Enum constructors are always private, so it is unnecessary to specify it.
     */
    REDUNDANT_MODIFIER_VISIBILITY_ENUM_CONSTRUCTOR,

    /**
     * Reports code where a method returns void and has an explicit return statement at the end.
     * <br>
     * The check is implemented by PMD, so it can break any time and it is difficult to fix false-positives.
     */
    @HasFalsePositives
    REDUNDANT_VOID_RETURN,

    /**
     * Reports code where a variable is assigned to itself.
     * <br>
     * Had quite a few false-positives in the past.
     */
    @HasFalsePositives
    REDUNDANT_SELF_ASSIGNMENT,

    /**
     * Reports code where a variable is declared only to be used once in the next line.
     * <br>
     * Sometimes one declares a variable so the line is not too long, this is tricky to ignore.
     */
    @HasFalsePositives
    REDUNDANT_VARIABLE,

    /**
     * Reports code where a boolean is compared to true or false. For example if (a == true) { ... } instead of if (a) { ... }.
     */
    REDUNDANT_BOOLEAN_EQUAL,

    /**
     * Reports code where an else block is redundant, because the if block always returns.
     * <br>
     * This can help with the readability of the code.
     * <br>
     * Likely has false positives, because of the way spoon represents if/else if/else blocks.
     */
    @HasFalsePositives
    REDUNDANT_ELSE,

    /**
     * Reports code where the Collection#addAll method could be used.
     */
    @HasFalsePositives
    SEQUENTIAL_ADD_ALL,

    /**
     * Reports cases where Pattern#compile is not in a static final field.
     */
    @HasFalsePositives
    AVOID_RECOMPILING_REGEX,

    /**
     * Finds imports that are not used.
     * <br>
     * This was very difficult to implement, found some bugs in the spoon library as well.
     * Very likely that this has false-positives (take a look at the tests).
     */
    @HasFalsePositives
    UNUSED_IMPORT,

    /**
     * Reports code where the primitive wrapper classes are instantiated explicitly. For example new Integer(5) instead of 5.
     */
    @HasFalsePositives
    PRIMITIVE_WRAPPER_INSTANTIATION,

    /**
     * Reports all occurrences of the assert keyword.
     * <br>
     * One of the simplest checks in the whole project, should not have any false-positives.
     */
    ASSERT,

    /**
     * Reports all occurrences where Exception.printStackTrace is called.
     * <br>
     * This is supposed to be used for debugging only. Check should not have any false-positives.
     */
    EXCEPTION_PRINT_STACK_TRACE,

    /**
     * Reports all occurrences where an exception inherits from RuntimeException instead of Exception.
     */
    CUSTOM_EXCEPTION_INHERITS_RUNTIME_EXCEPTION,

    /**
     * Reports all occurrences where an exception inherits from Error.
     */
    CUSTOM_EXCEPTION_INHERITS_ERROR,

    /**
     * Reports try-catch blocks where the catch block is empty (i.e. catch (Exception e) { }).
     */
    EMPTY_CATCH,

    /**
     * Reports all occurrences where an exception is thrown in a try-catch block, only to be caught by that try-catch block.
     */
    @HasFalsePositives
    EXCEPTION_CAUGHT_IN_SURROUNDING_BLOCK,

    /**
     * Reports catch blocks where an exception like ArrayIndexOutOfBoundsException is caught.
     */
    @HasFalsePositives
    EXCEPTION_SHOULD_NEVER_BE_CAUGHT,

    /**
     * Reports occurrences where a runtime exception is caught.
     * <br>
     * This check has "false-positives" because it is not always wrong to catch a runtime exception.
     * For example NumberFormatException or DateTimeParseException. Depending on the assignment new
     * packages are allowed, so this will always have some false-positives.
     */
    @HasFalsePositives
    RUNTIME_EXCEPTION_CAUGHT,

    /**
     * Reports code where two objects are compared through their toString method instead of equals. For example a.toString().equals(b.toString()).
     */
    @HasFalsePositives
    OBJECTS_COMPARED_VIA_TO_STRING,

    /**
     * Reports code where a field could be static final.
     * <br>
     * This check relies on a correct implementation of the final check (which is impossibly difficult).
     * Therefore, this check will have false-positives.
     */
    @HasFalsePositives
    FIELD_SHOULD_BE_CONSTANT,

    /**
     * Tries to find classes that are exclusively used for constants.
     * <br>
     * Difficult to detect reliably and everyone has a different opinion on what a constant class is.
     * Therefore, this check has false-positives.
     */
    @HasFalsePositives
    DO_NOT_HAVE_CONSTANTS_CLASS,

    /**
     * Reports code where the type parameter of a generic class are omitted. For example new ArrayList() instead of new ArrayList<String>().
     * <br>
     * This check likely has false-positives, because of the way spoon works. It also does not work with arrays {@link java.util.ArrayList[]},
     * because of the way spoon represents them.
     */
    @HasFalsePositives
    DO_NOT_USE_RAW_TYPES,

    /**
     * Reports duplicate code.
     * <br>
     * Very experimental at the moment.
     */
    @HasFalsePositives
    DUPLICATE_CODE,

    /**
     * Reports projects where there are too many classes in one package.
     * <br>
     * Difficult to set a good threshold -> has false-positives.
     */
    @HasFalsePositives
    TOO_FEW_PACKAGES,

    /**
     * Reports very large try-catch blocks.
     * <br>
     * It does not check if the try block can be made smaller and its existence is questionable.
     * See the relevant issues <a href="https://github.com/Feuermagier/autograder/issues/515">Feuermagier/autograder#515</a>
     * and <a href="https://github.com/Feuermagier/autograder/issues/530">Feuermagier/autograder#530</a>
     */
    @HasFalsePositives
    TRY_CATCH_COMPLEXITY,

    /**
     * Reports static blocks in classes.
     */
    AVOID_STATIC_BLOCKS,

    /**
     * Reports if a method can be static.
     * <br>
     * The implementation is tricky, therefore it likely has false-positives.
     */
    @HasFalsePositives
    METHOD_SHOULD_BE_STATIC,

    /**
     * Reports if a private method can be static.
     * <br>
     * The implementation is tricky, therefore it likely has false-positives.
     */
    @HasFalsePositives
    METHOD_SHOULD_BE_STATIC_NOT_PUBLIC,

    /**
     * Reports if a parameter is reassigned.
     * <br>
     * Variables can hide each other, maybe has false-positives.
     */
    @HasFalsePositives
    REASSIGNED_PARAMETER,

    /**
     * Reports double brace initialization.
     * <br>
     * Maybe has false-positives, almost nobody uses this syntax.
     */
    @HasFalsePositives
    DOUBLE_BRACE_INITIALIZATION,

    /**
     * Reports for loops that can be replaced with a for-each loop.
     * <br>
     * This check is not trivial and likely has false-positives.
     */
    @HasFalsePositives
    FOR_CAN_BE_FOREACH,

    /**
     * Reports loops that can be replaced with a do-while loop.
     * <br>
     * This check is not trivial and likely has false-positives.
     */
    @HasFalsePositives
    LOOP_SHOULD_BE_DO_WHILE,

    /**
     * Reports loops that can be replaced with a for loop.
     * <br>
     * This check is not trivial and likely has false-positives.
     */
    @HasFalsePositives
    LOOP_SHOULD_BE_FOR,

    /**
     * Reports methods that do not have an override annotation, but override a method.
     * <br>
     * Had false positives in the past.
     */
    OVERRIDE_ANNOTATION_MISSING,

    /**
     * Reports code where '\n' or '\r\n' is used instead of System.lineSeparator().
     */
    @HasFalsePositives
    SYSTEM_SPECIFIC_LINE_BREAK,

    /**
     * Reports boolean getters that are called getXy instead of isXy.
     */
    BOOLEAN_GETTER_NOT_CALLED_IS,

    /**
     * Similar to {@link ProblemType#CONSTANT_NAME_CONTAINS_VALUE}, but reports constant names that are exactly the value.
     */
    @HasFalsePositives
    MEANINGLESS_CONSTANT_NAME,

    /**
     * Reports identifiers that are badly named. Like a void method called getSomething.
     * <br>
     * Unlikely to have false-positives.
     */
    CONFUSING_IDENTIFIER,

    /**
     * Reports variables that consist of only one letter.
     */
    SINGLE_LETTER_LOCAL_NAME,

    /**
     * Tries to find abbreviations in identifiers.
     * <br>
     * This check is very difficult to implement and likely has false-positives.
     */
    @HasFalsePositives
    IDENTIFIER_IS_ABBREVIATED_TYPE,

    /**
     * Reports identifiers that contain the type in their name.
     * <br>
     * Sometimes it is coincidental that the type is in the name, so this check has false-positives.
     * Like a {@code List<Assignments> todoList}
     */
    @HasFalsePositives
    IDENTIFIER_CONTAINS_TYPE_NAME,

    /**
     * Suggests simplifications to big if-else chains.
     * <br>
     * Has never been used, because it reports too many things and it is difficult to write good suggestions that are readable.
     */
    @HasFalsePositives
    USE_GUARD_CLAUSES,

    /**
     * Reports code where a concrete collection is used as a field or return value instead of the interface.
     * <br>
     * This check is not trivial, but hasn't had many false-positives in the past.
     */
    CONCRETE_COLLECTION_AS_FIELD_OR_RETURN_VALUE,

    /**
     * Reports code where a reference to a collection is returned without making a copy.
     * <br>
     * This check was very difficult to implement and likely has false-positives.
     */
    @HasFalsePositives
    LEAKED_COLLECTION_RETURN,

    /**
     * Reports code where a reference to a collection is assigned which enables mutation from the outside.
     * <br>
     * This check was very difficult to implement and likely has false-positives.
     */
    @HasFalsePositives
    LEAKED_COLLECTION_ASSIGN,

    /**
     * Reports code where the IDE placeholder implementation is used.
     * <br>
     * Not sure how well this works, because I have never seen this in the wild.
     */
    @HasFalsePositives
    METHOD_USES_PLACEHOLDER_IMPLEMENTATION,

    /**
     * Reports utility classes that are not final.
     */
    UTILITY_CLASS_NOT_FINAL,

    /**
     * Reports utility classes that have a public constructor or some other constructor.
     */
    @HasFalsePositives
    UTILITY_CLASS_INVALID_CONSTRUCTOR,

    /**
     * Reports classes that are in the default package.
     */
    DEFAULT_PACKAGE_USED,

    /**
     * Reports reimplementations of array copy.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_ARRAY_COPY,

    /**
     * Reports reimplementations of string repeat.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_STRING_REPEAT,

    /**
     * Reports reimplementations of Math.max/Math.min.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_MAX_MIN,

    /**
     * Reports reimplementations of Math.sqrt.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_SQRT,

    /**
     * Reports reimplementations of Math.hypot.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_HYPOT,

    /**
     * Reports loops where a method is repeatedly called like Collection#add,
     * which could be replaced with a single call to Collection#addAll.
     */
    @HasFalsePositives
    FOR_LOOP_CAN_BE_INVOCATION,

    /**
     * Reports code where Enum#values() could be used.
     */
    @HasFalsePositives
    USE_ENUM_VALUES,

    /**
     * Reports code where Arrays#fill could be used.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_ARRAYS_FILL,

    /**
     * Reports code where modulo could be used.
     */
    @HasFalsePositives
    USE_MODULO_OPERATOR,

    /**
     * Reports code where a sublist could be used.
     * <br>
     * Note that this check is quite pedantic.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_SUBLIST,

    /**
     * Reports code where one could use a single line to check for duplicates in an iterable.
     */
    @HasFalsePositives
    COMMON_REIMPLEMENTATION_ITERABLE_DUPLICATES,

    /**
     * Reports abstract classes that do not have any abstract methods.
     */
    @HasFalsePositives
    ABSTRACT_CLASS_WITHOUT_ABSTRACT_METHOD,

    /**
     * Reports abstract classes that only have methods, therefore they could be interfaces.
     */
    @HasFalsePositives
    SHOULD_BE_INTERFACE,

    /**
     * Reports where inheritance is wrong.
     */
    @HasFalsePositives
    COMPOSITION_OVER_INHERITANCE,

    /**
     * Suggests using entrySet.
     */
    @HasFalsePositives
    USE_ENTRY_SET,

    /**
     * Reports empty blocks without comments.
     */
    @HasFalsePositives
    EMPTY_BLOCK,

    /**
     * Reports unused code elements.
     * <br>
     * This check had by far the most false-positives, so much time went into this.
     */
    @HasFalsePositives
    UNUSED_CODE_ELEMENT,

    /**
     * Reports unused private code elements.
     * <br>
     * This is separate from {@link ProblemType#UNUSED_CODE_ELEMENT}, because some assignments might not have a main method
     * and instead program against an interface (then everything would be reported).
     */
    @HasFalsePositives
    UNUSED_CODE_ELEMENT_PRIVATE,

    /**
     * Reports identifiers that are similar to each other.
     */
    @HasFalsePositives
    SIMILAR_IDENTIFIER,

    /**
     * Reports code that could use multiplication or Math.pow.
     */
    @HasFalsePositives
    REPEATED_MATH_OPERATION,

    /**
     * Reports fields that could be instance fields.
     * <br>
     * Note that this check is more of a wildcard, it reports almost everything that is static and not final.
     */
    @HasFalsePositives
    STATIC_FIELD_SHOULD_BE_INSTANCE,

    /**
     * Reports fields that could be final.
     * <br>
     * Note that this check is very difficult to implement, likely to have false-positives.
     */
    @HasFalsePositives
    FIELD_SHOULD_BE_FINAL,

    /**
     * Strings should be compared through equals, not through ==.
     */
    STRING_COMPARE_BY_REFERENCE,

    /**
     * Reports code where !(a == b) is used instead of a != b and similar.
     */
    @HasFalsePositives
    REDUNDANT_NEGATION,

    /**
     * Reports code like a = a + 1 instead of a += 1.
     */
    USE_OPERATOR_ASSIGNMENT,

    /**
     * Reports code like else { if (a) { ... } } that could be else if (a) { ... }.
     */
    @HasFalsePositives
    UNMERGED_ELSE_IF,

    /**
     * Reports code like if (a) { if (b) { ... } } that could be if (a && b) { ... }.
     */
    @HasFalsePositives
    MERGE_NESTED_IF,

    /**
     * Reports code that throws an exception without a message.
     * <br>
     * Likely has false-positives.
     */
    @HasFalsePositives
    EXCEPTION_WITHOUT_MESSAGE,

    /**
     * Reports interfaces that have nothing in them.
     */
    EMPTY_INTERFACE,

    /**
     * Reports uses of SuppressWarnings.
     */
    SUPPRESS_WARNINGS_USED,

    /**
     * Reports complicated regular expressions that do not have comments or javadoc.
     * <br>
     * The check reliably detects regular expressions, but can not determine well if they are complicated.
     */
    @HasFalsePositives
    COMPLEX_REGEX,

    /**
     * Suggests using a format string.
     * <br>
     * Sometimes the check was a bit too pedantic.
     */
    @HasFalsePositives
    USE_FORMAT_STRING,

    /**
     * Reports code where a local variable could be a constant.
     */
    @HasFalsePositives
    LOCAL_VARIABLE_SHOULD_BE_CONSTANT,

    /**
     * Suggests using EnumMap or EnumSet.
     */
    @HasFalsePositives
    USE_ENUM_COLLECTION,

    /**
     * Reports uses of the instanceof operator.
     */
    @HasFalsePositives
    INSTANCEOF,

    /**
     * Reports code that effectively uses instanceof without using instanceof. Like getClass().equals.
     */
    @HasFalsePositives
    INSTANCEOF_EMULATION,

    /**
     * Reports code that compares characters through their numeric value instead of as chars.
     */
    @HasFalsePositives
    COMPARE_CHAR_VALUE,

    /**
     * Reports catch blocks that throw the same exception that they catch.
     */
    @HasFalsePositives
    REDUNDANT_CATCH,

    /**
     * Reports things that could be enum attributes.
     */
    @HasFalsePositives
    SHOULD_BE_ENUM_ATTRIBUTE,

    /**
     * Reports constants that should be represented as enums.
     */
    @HasFalsePositives
    CLOSED_SET_OF_VALUES,

    /**
     * Reports types with bad names.
     */
    @HasFalsePositives
    TYPE_HAS_DESCRIPTIVE_NAME,

    /**
     * Reports identifiers that have random trailing numbers like a1 instead of a.
     */
    @HasFalsePositives
    IDENTIFIER_REDUNDANT_NUMBER_SUFFIX,

    /**
     * Reports code where a fully qualified name is used instead of an import.
     * <br>
     * This check has false-positives, because of how spoon represents stuff.
     */
    @HasFalsePositives
    IMPORT_TYPES,

    /**
     * Suggests using a different visibility modifier. (Things that can be private)
     * <br>
     * This check is very difficult to implement and likely has false-positives.
     */
    @HasFalsePositives
    USE_DIFFERENT_VISIBILITY,

    /**
     * Suggests using a different visibility modifier. (Things that can be default visibility or protected)
     * <br>
     * This check is very difficult to implement and likely has false-positives.
     */
    @HasFalsePositives
    USE_DIFFERENT_VISIBILITY_PEDANTIC,

    /**
     * Reports any non static final field that is public.
     */
    USE_DIFFERENT_VISIBILITY_PUBLIC_FIELD,

    /**
     * Reports multi-threading code.
     */
    @HasFalsePositives
    MULTI_THREADING,

    /**
     * The result of #compareTo or #compare should only be compared to 0.
     * It is an implementation detail whether a given type returns strictly the values {-1, 0, +1} or others.
     * <br>
     * https://errorprone.info/bugpattern/CompareToZero
     */
    @HasFalsePositives
    COMPARE_TO_ZERO,

    /**
     * Implementing #equals by just comparing hashCodes is fragile.
     */
    @HasFalsePositives
    EQUALS_USING_HASHCODE,

    /**
     * The contract of #equals states that it should return false for incompatible types,
     */
    @HasFalsePositives
    EQUALS_UNSAFE_CAST,

    /**
     * An equality test between objects with incompatible types always returns false
     */
    @HasFalsePositives
    EQUALS_INCOMPATIBLE_TYPE,

    /**
     * Including fields in hashCode which are not compared in equals violates the contract of hashCode.
     */
    @HasFalsePositives
    INCONSISTENT_HASH_CODE,

    /**
     * This type is not guaranteed to implement a useful #equals method.
     */
    @HasFalsePositives
    UNDEFINED_EQUALS,

    /**
     * Reports equals implementations that are broken for null.
     */
    @HasFalsePositives
    EQUALS_BROKEN_FOR_NULL,

    /**
     * Reports equals methods that do not override Object#equals.
     */
    @HasFalsePositives
    NON_OVERRIDING_EQUALS,

    /**
     * Hashcode method on array does not hash array contents
     */
    @HasFalsePositives
    ARRAYS_HASHCODE,

    /**
     * == must be used in equals method to check equality to itself or an infinite loop will occur.
     */
    @HasFalsePositives
    EQUALS_REFERENCE,

    /**
     * Arrays do not override equals or hashCode, so comparisons will be done on reference equality
     * only. If neither deduplication nor lookup are needed, consider using a List instead.
     */
    @HasFalsePositives
    ARRAY_AS_KEY_OF_SET_OR_MAP,

    /**
     * Reports code with multiple statements on one line. Like declaring multiple variables in one line.
     */
    @HasFalsePositives
    MULTIPLE_INLINE_STATEMENTS,

    /**
     * Reports code where the primitive type could be used instead of the wrapper type.
     */
    @HasFalsePositives
    UNNECESSARY_BOXING,

    /**
     * Reports any use of String#concat, just use + instead.
     */
    @HasFalsePositives
    AVOID_STRING_CONCAT,

    /**
     * Reports unnecessary comments.
     */
    @HasFalsePositives
    UNNECESSARY_COMMENT,

    /**
     * Reports any uses of Object as a datatype.
     */
    @HasFalsePositives
    OBJECT_DATATYPE,

    /**
     * Reports code where a NumberFormatException is never caught.
     */
    @HasFalsePositives
    NUMBER_FORMAT_EXCEPTION_IGNORED,

    /**
     * Reports code where a variable is declared, but not initialized.
     */
    @HasFalsePositives
    REDUNDANT_UNINITIALIZED_VARIABLE;

    /**
     * <strong>Used via reflection, so don't remove, even if your IDE shows no usages!</strong>
     * @param name
     * @return
     */
    public static ProblemType fromString(String name) {
        return ProblemType.valueOf(name);
    }
}
