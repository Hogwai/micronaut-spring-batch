package com.hogwai.batch.core.runtime;

/**
 * Represents the outcome of a step or job execution as an exit code with an optional description.
 *
 * @param exitCode        the machine-readable exit code (e.g. "COMPLETED", "FAILED")
 * @param exitDescription a human-readable description of the exit status
 */
public record ExitStatus(String exitCode, String exitDescription) {
    /** Exit status indicating successful completion. */
    public static final ExitStatus COMPLETED = new ExitStatus("COMPLETED", "");
    public static final String FAILED_LABEL = "FAILED";
    /** Exit status indicating failure. */
    public static final ExitStatus FAILED = new ExitStatus(FAILED_LABEL, "");
    public static final String STOPPED_LABEL = "STOPPED";
    /** Exit status indicating the execution was stopped. */
    public static final ExitStatus STOPPED = new ExitStatus(STOPPED_LABEL, "");
    /** Exit status indicating an unknown outcome. */
    public static final ExitStatus UNKNOWN = new ExitStatus("UNKNOWN", "");

    /**
     * Creates an exit status with the given code and an empty description.
     *
     * @param exitCode the exit code
     */
    public ExitStatus(String exitCode) {
        this(exitCode, "");
    }

    /**
     * Combines this exit status with another, returning the most severe.
     * FAILED takes precedence over STOPPED, which takes precedence over other statuses.
     *
     * @param other the other exit status to combine with
     * @return the combined exit status reflecting the worst outcome
     */
    public ExitStatus and(ExitStatus other) {
        if (FAILED_LABEL.equals(other.exitCode) || FAILED_LABEL.equals(this.exitCode)) {
            return FAILED;
        }
        if (STOPPED_LABEL.equals(other.exitCode) || STOPPED_LABEL.equals(this.exitCode)) {
            return STOPPED;
        }
        return this;
    }
}
