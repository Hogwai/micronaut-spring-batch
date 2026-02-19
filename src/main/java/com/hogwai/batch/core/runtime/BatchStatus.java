package com.hogwai.batch.core.runtime;

/**
 * Enumeration of possible batch execution states for jobs and steps.
 */
public enum BatchStatus {
    /** The execution is being initialized. */
    STARTING,
    /** The execution is actively running. */
    STARTED,
    /** The execution finished successfully. */
    COMPLETED,
    /** The execution terminated due to an error. */
    FAILED,
    /** The execution is in the process of stopping. */
    STOPPING,
    /** The execution was explicitly stopped before completion. */
    STOPPED;
}
