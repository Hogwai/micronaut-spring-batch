package com.hogwai.batch.core.runtime;

import com.hogwai.batch.core.config.JobParameters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracks the runtime state of a job execution, including status, timing, and child step executions.
 *
 * @see JobInstance
 * @see StepExecution
 */
public class JobExecution {
    private final long id;
    private final JobInstance jobInstance;
    private final JobParameters jobParameters;
    private volatile BatchStatus status = BatchStatus.STARTING;
    private volatile ExitStatus exitStatus = ExitStatus.UNKNOWN;
    private volatile Instant startTime;
    private volatile Instant endTime;
    private final ExecutionContext executionContext = new ExecutionContext();
    private final List<StepExecution> stepExecutions = new ArrayList<>();

    /**
     * Creates a new job execution.
     *
     * @param id            the unique execution identifier
     * @param jobInstance   the job instance this execution belongs to
     * @param jobParameters the parameters used to launch this execution
     */
    public JobExecution(long id, JobInstance jobInstance, JobParameters jobParameters) {
        this.id = id;
        this.jobInstance = jobInstance;
        this.jobParameters = jobParameters;
    }

    /**
     * Creates and registers a new step execution under this job execution.
     *
     * @param stepExecId the unique step execution identifier
     * @param stepName   the name of the step
     * @return the newly created step execution
     */
    public StepExecution createStepExecution(long stepExecId, String stepName) {
        StepExecution stepExecution = new StepExecution(stepExecId, stepName, this);
        stepExecutions.add(stepExecution);
        return stepExecution;
    }

    /** @return the unique execution identifier */
    public long getId() { return id; }

    /** @return the job instance this execution belongs to */
    public JobInstance getJobInstance() { return jobInstance; }

    /** @return the parameters used to launch this execution */
    public JobParameters getJobParameters() { return jobParameters; }

    /** @return the current batch status */
    public BatchStatus getStatus() { return status; }

    /** @param status the batch status to set */
    public void setStatus(BatchStatus status) { this.status = status; }

    /** @return the current exit status */
    public ExitStatus getExitStatus() { return exitStatus; }

    /** @param exitStatus the exit status to set */
    public void setExitStatus(ExitStatus exitStatus) { this.exitStatus = exitStatus; }

    /** @param startTime the start time to set */
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    /** @param endTime the end time to set */
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    /** @return the time this execution started, or {@code null} if not yet started */
    public Instant getStartTime() { return startTime; }

    /** @return the time this execution ended, or {@code null} if still running */
    public Instant getEndTime() { return endTime; }

    /** @return the execution context for sharing data across steps */
    public ExecutionContext getExecutionContext() { return executionContext; }

    /**
     * Returns an unmodifiable view of the step executions belonging to this job execution.
     *
     * @return the list of step executions
     */
    public List<StepExecution> getStepExecutions() { return Collections.unmodifiableList(stepExecutions); }
}
