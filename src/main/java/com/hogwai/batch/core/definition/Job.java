package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.listener.JobExecutionListener;

import java.util.List;

/**
 * Defines a batch job composed of one or more {@link Step}s executed sequentially.
 */
public interface Job {
    /**
     * Returns the name of this job.
     *
     * @return the job name
     */
    String getName();

    /**
     * Returns the ordered list of steps that make up this job.
     *
     * @return the steps to execute
     */
    List<Step> getSteps();

    /**
     * Returns the listeners to be notified of job-level lifecycle events.
     *
     * @return the job execution listeners, empty by default
     */
    default List<JobExecutionListener> getListeners() { return List.of(); }
}
