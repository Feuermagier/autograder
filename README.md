## Automatically run static & dynamic analysis on student code to aid grading.

### All currently implemented checks can be found [here](checks.md)

### Usage

Autograder requires at least Java 17.
Make sure Docker is running if you want to use the dynamic analysis.
The main class of the command line application is [de.firemage.autograder.cmd.Application](autograder-cmd/src/main/java/de/firemage/autograder/cmd/Application.java).
Configuration is done using command line flags and a check config file.
The command line parameters are
* Parameter #1: The path to the check config file
* Parameter #2: The path to the student's code. The directory should contain the root package. Non-java files are ignored.
* Parameter #3: (Optional) The path to the directory containing the test protocols. Those are used for the dynamic analysis. If not specified, no tests will be run.
* `-j <int>` / `--java <int>`: Specify the java version of the student's code. Default: `11`
* `-s` / `--static`: Disable dynamic analysis.

The check config file must be a valid YAML file that configures the tests to run.
Autograder can also eb used as a library by just using the module `autograder-core`.

### Additional information

The code is analyzed using PMD, SpotBugs and the PMD CPD with custom configurations as well as custom static and dynamic checks ("integrated" checks).
You can find an exemplary config file [here](sample_config.yaml).
If you have an idea for another check open an issue, contact me or implement the check yourself and open a pull request.

The dynamic analysis executes the student's code in secured Docker containers.
We never execute any foreign code on the host!
However, this means that you need to have Docker installed and running if you want to use the dynamic analysis.
If you see strange BrokenPipeErrors, restart your Docker daemon.
Dynamic analysis is currently only available for command line I/O based programs.
Support for JUnit tests is planned.

If you see a VerifyError in the console, please open an issue or contact me directly.
The dynamic analysis modifies the bytecode of the student's code, and bugs typically result in invalid bytecode.

Note: To get accurate results from the dynamic analysis, make sure that the tests cover most or all possible input types.
This makes sure that all possible code paths are executed.

### Development
As of now, the following tools can be used in checks:
* PMD XPath expressions to query the source code. 
When reasonably possible, those should be used to keep the code clean.
* A model of the source code built with [Spoon](https://github.com/INRIA/spoon). Most non-trivial checks use this model.
* An (unstructured) list of events generated during the test runs.
* A dependency graph between classes of the source code, built with [JGraphT](https://jgrapht.org/).
* (not yet implemented) A full static dataflow analysis built with [Soot](https://github.com/soot-oss/soot) and [Heros](https://github.com/Sable/heros).

All checks except the PMD-based checks must inherit from PMD-based tests must inherit from [IntegratedCheck](autograder-core/src/main/java/de/firemage/autograder/core/integrated/IntegratedCheck.java).
PMD-based checks must inherit from [PMDCheck](autograder-core/src/main/java/de/firemage/autograder/core/pmd/PMDCheck.java).

If you change the executor or the agent, run `mvn compile` for all so that the jar files in [autograder-core resources](autograder-core) get updated.

The directory [test_submissions](test_submissions) contains two projects you can test your checks on:
* [Bank](test_submissions/Bank) is an implementation of a task I used in my tutorial.
  The code was intended to show the separation between logic and UI and is therefore not submission ready (e.g. JavaDoc is missing).
* [A1](test_submissions/A1) is my own submission to a final task at my university.

There is also simple testing framework for checks implemented in the [test directory](autograder-core/src/test/java).

### Compiling the project

The project uses Maven. To compile the project, run
```
$ mvn package -DskipTests --file pom.xml
```
in the root directory.

The jar will be at `autograder-cmd/target/autograder-cmd.jar`.
