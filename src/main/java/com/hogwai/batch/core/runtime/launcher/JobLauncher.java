package com.hogwai.batch.core.runtime.launcher;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.runtime.JobExecution;

/**
 * Entry point for launching a job with a given set of parameters.
 */
public interface JobLauncher {

    /**
     * Launches the specified job with the given parameters.
     *
     * @param job           the job to execute
     * @param jobParameters the parameters for this execution
     * @return the resulting job execution with status and metrics
     * @throws Exception if the job fails during execution
     */
    JobExecution run(Job job, JobParameters jobParameters) throws Exception;
}