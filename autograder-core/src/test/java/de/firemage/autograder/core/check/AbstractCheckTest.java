package de.firemage.autograder.core.check;

import de.firemage.autograder.core.Linter;
import de.firemage.autograder.core.LinterException;
import de.firemage.autograder.core.Problem;
import de.firemage.autograder.core.ProblemType;
import de.firemage.autograder.core.SourceInfo;
import de.firemage.autograder.core.errorprone.TempLocation;
import de.firemage.autograder.core.file.UploadedFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class AbstractCheckTest {
    protected final TempLocation tempLocation;
    protected final Linter linter;

    protected AbstractCheckTest() {
        this(TempLocation.random());
    }

    private AbstractCheckTest(TempLocation tempLocation) {
        this.tempLocation = tempLocation;
        this.linter = Linter.builder(Locale.US)
            .tempLocation(this.tempLocation)
            .build();
    }

    protected List<Problem> check(
        SourceInfo sourceInfo,
        List<ProblemType> problemTypes
    ) throws LinterException, IOException {
        return this.linter.checkFile(
            UploadedFile.build(sourceInfo, this.tempLocation.toPath(), status -> {
            }),
            null,
            new ArrayList<>(problemTypes),
            status -> {
            }
        );
    }
}
