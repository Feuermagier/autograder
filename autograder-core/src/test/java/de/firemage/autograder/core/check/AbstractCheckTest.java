package de.firemage.autograder.core.check;

import de.firemage.autograder.api.CheckConfiguration;
import de.firemage.autograder.api.Linter;
import de.firemage.autograder.core.LinterImpl;
import de.firemage.autograder.api.LinterException;
import de.firemage.autograder.api.Problem;
import de.firemage.autograder.api.ProblemType;
import de.firemage.autograder.core.ProblemImpl;
import de.firemage.autograder.core.file.SourceInfo;
import de.firemage.autograder.api.TempLocation;
import de.firemage.autograder.core.file.TempLocationImpl;
import de.firemage.autograder.core.file.UploadedFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractCheckTest {
    protected final TempLocation tempLocation;
    protected final LinterImpl linter;

    protected AbstractCheckTest() {
        this(-1);
    }

    protected AbstractCheckTest(int limit) {
        this(new TempLocationImpl(), limit);
    }

    private AbstractCheckTest(TempLocation tempLocation, int limit) {
        this.tempLocation = tempLocation;
        this.linter = new LinterImpl(Linter.builder(Locale.US)
            .tempLocation(this.tempLocation)
            .maxProblemsPerCheck(limit)
            .threads(1));
    }

    protected List<ProblemImpl> check(
        SourceInfo sourceInfo,
        List<ProblemType> problemTypes
    ) throws LinterException, IOException {
        return this.linter.checkFile(
            UploadedFile.build(sourceInfo, this.tempLocation, status -> {
            }, null),
            CheckConfiguration.fromProblemTypes(problemTypes),
            status -> {
            }
        );
    }

    protected ProblemIterator checkIterator(
        SourceInfo sourceInfo,
        List<ProblemType> problemTypes
    ) throws LinterException, IOException {
        return new ProblemIterator(this.check(sourceInfo, problemTypes));
    }

    public static final class ProblemIterator implements Iterator<Problem> {
        private final List<? extends Problem> problems;
        private int index;

        private ProblemIterator(List<? extends Problem> problems) {
            this.problems = problems;
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.problems.size();
        }

        @Override
        public Problem next() throws NoSuchElementException {
            if (!this.hasNext()) {
                throw new NoSuchElementException(
                    "Expected at least %d problems, but got %d. Problems: %s".formatted(
                        this.index + 1, this.problems.size(), this.problems
                    )
                );
            }

            this.index += 1;
            return this.problems.get(this.index - 1);
        }

        public void assertExhausted() {
            if (this.hasNext()) {
                fail("Expected exactly %d problems, but got %d. Extra problem(s): %s".formatted(
                    this.index, this.problems.size(), this.problems.subList(this.index, this.problems.size())
                ));
            }
        }
    }

    protected Map.Entry<String, String> dummySourceEntry(String packageName, String className) {
        String name = packageName.isEmpty() ? className : packageName + "." + className;
        return Map.entry(
            name,
            """
            %spublic class %s {}
            """.formatted(packageName.isEmpty() ? "" : "package %s;\n\n".formatted(packageName), className)
        );
    }
}
