package com.hogwai.batch.core.runtime;

public record ExitStatus(String exitCode, String exitDescription) {
    public static final ExitStatus COMPLETED = new ExitStatus("COMPLETED", "");
    public static final ExitStatus FAILED = new ExitStatus("FAILED", "");
    public static final ExitStatus STOPPED = new ExitStatus("STOPPED", "");
    public static final ExitStatus UNKNOWN = new ExitStatus("UNKNOWN", "");

    public ExitStatus(String exitCode) {
        this(exitCode, "");
    }

    public ExitStatus and(ExitStatus other) {
        if ("FAILED".equals(other.exitCode) || "FAILED".equals(this.exitCode)) {
            return FAILED;
        }
        if ("STOPPED".equals(other.exitCode) || "STOPPED".equals(this.exitCode)) {
            return STOPPED;
        }
        return this;
    }
}
