package de.firemage.autograder.core;

public enum LinterStatus {
    COMPILING(new LocalizedMessage("status-compiling")),
    RUNNING_SPOTBUGS(new LocalizedMessage("status-spotbugs")),
    RUNNING_PMD(new LocalizedMessage("status-pmd")),
    RUNNING_CPD(new LocalizedMessage("status-cpd")),
    RUNNING_ERROR_PRONE(new LocalizedMessage("status-error-prone")),
    BUILDING_CODE_MODEL(new LocalizedMessage("status-model")),
    BUILDING_DOCKER_IMAGE(new LocalizedMessage("status-docker")),
    EXECUTING_TESTS(new LocalizedMessage("status-tests")),
    RUNNING_INTEGRATED_CHECKS(new LocalizedMessage("status-integrated"));

    private final LocalizedMessage message;


    LinterStatus(LocalizedMessage message) {
        this.message = message;
    }

    public LocalizedMessage getMessage() {
        return message;
    }
}
