package com.hogwai.batch.core.listener;

import com.hogwai.batch.core.runtime.JobExecution;

/**
 * Listener interface for receiving callbacks on job-level lifecycle events.
 *
 * @see JobExecution
 */
public interface JobExecutionListener {

    /**
     * Called before a job begins execution.
     *
     * @param jobExecution the current job execution context
     */
    default void beforeJob(JobExecution jobExecution) {}

    /**
     * Called after a job has completed (regardless of success or failure).
     *
     * @param jobExecution the current job execution context
     */
    default void afterJob(JobExecution jobExecution) {}
}
