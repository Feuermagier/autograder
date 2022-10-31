package de.firemage.autograder.core.check;


import de.firemage.autograder.core.LocalizedMessage;

public interface Check {
    LocalizedMessage getDescription();

    LocalizedMessage getLinter();
}
