package com.hogwai.batch.core.runtime;

import java.time.Instant;

/**
 * Tracks the runtime state and metrics of a single step execution within a job.
 *
 * @see StepContribution
 * @see JobExecution
 */
public class StepExecution {
    private final long id;
    private final String stepName;
    private final JobExecution jobExecution;
    private volatile BatchStatus status = BatchStatus.STARTING;
    private volatile ExitStatus exitStatus = ExitStatus.UNKNOWN;
    private volatile Instant startTime;
    private volatile Instant endTime;
    private final ExecutionContext executionContext = new ExecutionContext();

    private long readCount;
    private long writeCount;
    private long commitCount;
    private long rollbackCount;
    private long filterCount;
    private long skipCount;

    /**
     * Creates a new step execution.
     *
     * @param id           the unique execution identifier
     * @param stepName     the name of the step being executed
     * @param jobExecution the parent job execution
     */
    public StepExecution(long id, String stepName, JobExecution jobExecution) {
        this.id = id;
        this.stepName = stepName;
        this.jobExecution = jobExecution;
    }

    /**
     * Applies metrics from a {@link StepContribution} to this execution,
     * adding counts and updating the exit status.
     *
     * @param contribution the contribution to apply
     */
    public void apply(StepContribution contribution) {
        this.readCount += contribution.getReadCount();
        this.writeCount += contribution.getWriteCount();
        this.filterCount += contribution.getFilterCount();
        this.skipCount += contribution.getSkipCount();
        this.exitStatus = contribution.getExitStatus();
    }

    /** Increments the commit count by one. */
    public void incrementCommitCount() { commitCount++; }

    /** Increments the rollback count by one. */
    public void incrementRollbackCount() { rollbackCount++; }

    /** @return the unique execution identifier */
    public long getId() { return id; }

    /** @return the name of the step */
    public String getStepName() { return stepName; }

    /** @return the parent job execution */
    public JobExecution getJobExecution() { return jobExecution; }

    /** @return the current batch status */
    public BatchStatus getStatus() { return status; }

    /**
     * @param status the batch status to set
     */
    public void setStatus(BatchStatus status) { this.status = status; }

    /** @return the current exit status */
    public ExitStatus getExitStatus() { return exitStatus; }

    /**
     * @param exitStatus the exit status to set
     */
    public void setExitStatus(ExitStatus exitStatus) { this.exitStatus = exitStatus; }

    /** @return the time this step started, or {@code null} if not yet started */
    public Instant getStartTime() { return startTime; }

    /**
     * @param startTime the start time to set
     */
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    /** @return the time this step ended, or {@code null} if still running */
    public Instant getEndTime() { return endTime; }

    /**
     * @param endTime the end time to set
     */
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    /** @return the execution context for sharing data within this step */
    public ExecutionContext getExecutionContext() { return executionContext; }

    /** @return the total number of items read */
    public long getReadCount() { return readCount; }

    /** @return the total number of items written */
    public long getWriteCount() { return writeCount; }

    /** @return the total number of chunk commits */
    public long getCommitCount() { return commitCount; }

    /** @return the total number of chunk rollbacks */
    public long getRollbackCount() { return rollbackCount; }

    /** @return the total number of items filtered out */
    public long getFilterCount() { return filterCount; }

    /** @return the total number of skipped items */
    public long getSkipCount() { return skipCount; }
}
