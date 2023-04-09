## All currently implemented checks

### General Checks
* For-loop should be for each loop
* @Override missing
* Field should be a local variable (a bit flaky rn, may produce false positives)
* Constants should be `static final` and have a UPPER_SNAKE_CASE name
* Double-brace initialization used
* Equals does not check for null values
* Objects should be compared with equals and not via toString
* Method parameters shouldn't be reassigned
* Attribute should be a local variable
* System-dependent linebreak used in string literal

### Java API
* isEmpty should be used on Strings/Collections instead of comparing the length/size against 0
* Old/Deprecated collections such as Stack shouldn't be used

### Unnecessary Complexity
* Class explicitly extends Object
* Diamond Operator should be used
* Redundant modifiers
* Redundant returns
* Unnecessary local creation before returns (`int x = 0; return x;`)
* Unused imports (sometimes also reports star imports)
* Instantiation of primitive wrappers
* Redundant default constructor (this check may report default constructors with custom JavaDoc)
* For loop should have exactly one loop variable
* Empty non-catch block
* All locals, parameters, private fields & private methods should be used
* Unnecessary if as the value of the condition should be directly used (e.g. `if (x) { return true; } else { false; }`)
* Variable is reassigned to itself
* Overwriting default values in arrays with default values
* Not using operator assignments (e.g. `x = x + 1` instead of `x += 1`)

### Debugging Artifacts
* Assertions used
* Exception#printStackTrace used

### Exceptions
* Empty catch block
* Runtime exception or error is caught
* Custom exception inherits from RuntimeException or Error
* Exception is thrown and immediately caught in the same method

### Comments & JavaDoc
* Method returns null in some tests but the Javadoc doesn't mention it
* Not all comments are either in German or in English (ignores very short comments)
* JavaDoc contains tags with empty descriptions
* Commented out code should be removed
* The @author tag should match a specified Regex
* JavaDoc for methods should mention all parameters

### Naming
* Getters for Booleans should be named `isXYZ`
* Method names should follow linguistic conventions (see the [PMD documentation](https://pmd.github.io/latest/pmd_rules_java_codestyle.html))
* Variables should not have overly short names or be abbreviations of their types
* Constants should have descriptive names (i.e. not `PARAM_ONE` for `1`)

### OOP
* Methods should never return concrete collection types but e.g. `List` or `Set`. The same applies to fields.
* Methods in abstract classes should be abstract instead of providing a dummy implementation
* Mutable lists in fields should be copied before returning them
* Utility classes should have only final fields and a single private no-arg constructor
* Interfaces should not have fields, constants or be empty
* Input/Output should be separated from the program logic
* `System.exit()` should never be used

### Code Structure
* The default package should not be used
