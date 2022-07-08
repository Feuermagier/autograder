package de.firemage.autograder.core.spoon.check;

import de.firemage.autograder.core.Problem;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;
import java.util.List;

public interface CodeProcessor {
    List<Problem> check(CtModel model, Factory factory);
}
