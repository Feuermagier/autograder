package de.firemage.codelinter.core.check.exceptions;

import de.firemage.codelinter.core.pmd.PMDCheck;

public class EmptyCatchCheck extends PMDCheck {
    private static final String DESCRIPTION = """
            There is no reason to have an empty catch block in your program. 
            If you are sure that the caught exception will never be thrown, throw an IllegalStateException in the catch block.""";

    public EmptyCatchCheck() {
        super(DESCRIPTION, createXPathRule("empty catch", "Empty catch block", "//CatchStatement[not(Block/BlockStatement)]"));
    }
}
