package de.firemage.autograder.cmd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class A1Test {
    @Test
    void testA1() {
        // Only checks whether the autograder fails
        int returnCode = Application.runApplication("../sample_config.yaml", "../test_submissions/A1/code", "-j", "17", "-s");
        assertEquals(0, returnCode);
    }
}
