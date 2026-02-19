package com.hogwai.batch.core.listener;

import com.hogwai.batch.core.runtime.StepExecution;

/**
 * Listener interface for receiving callbacks on step-level lifecycle events.
 *
 * @see StepExecution
 */
public interface StepExecutionListener {

    /**
     * Called before a step begins execution.
     *
     * @param stepExecution the current step execution context
     */
    default void beforeStep(StepExecution stepExecution) {}

    /**
     * Called after a step has completed (regardless of success or failure).
     *
     * @param stepExecution the current step execution context
     */
    default void afterStep(StepExecution stepExecution) {}
}
