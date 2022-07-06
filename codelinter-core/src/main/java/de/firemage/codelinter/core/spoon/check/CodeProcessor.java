package de.firemage.codelinter.core.spoon.check;

import de.firemage.codelinter.core.Problem;
import spoon.reflect.CtModel;
import spoon.reflect.factory.Factory;
import java.util.List;

public interface CodeProcessor {
    List<Problem> check(CtModel model, Factory factory);
}
