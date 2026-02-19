package com.hogwai.batch.core.runtime.repository;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.JobInstance;
import com.hogwai.batch.core.runtime.StepExecution;

/**
 * Persistence interface for creating and updating job and step execution metadata.
 */
public interface JobRepository {

    /**
     * Creates a new job instance for the given job name.
     *
     * @param jobName       the name of the job
     * @param jobParameters the parameters identifying this instance
     * @return the newly created job instance
     */
    JobInstance createJobInstance(String jobName, JobParameters jobParameters);

    /**
     * Creates a new job execution for the given job instance.
     *
     * @param jobInstance   the job instance to create an execution for
     * @param jobParameters the parameters for this execution
     * @return the newly created job execution
     */
    JobExecution createJobExecution(JobInstance jobInstance, JobParameters jobParameters);

    /**
     * Creates a new step execution under the given job execution.
     *
     * @param jobExecution the parent job execution
     * @param stepName     the name of the step
     * @return the newly created step execution
     */
    StepExecution createStepExecution(JobExecution jobExecution, String stepName);

    /**
     * Persists updated state for the given job execution.
     *
     * @param execution the job execution to update
     */
    void update(JobExecution execution);

    /**
     * Persists updated state for the given step execution.
     *
     * @param stepExecution the step execution to update
     */
    void update(StepExecution stepExecution);
}
