package com.hogwai.batch.core.runtime;

import java.time.Instant;

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

    public StepExecution(long id, String stepName, JobExecution jobExecution) {
        this.id = id;
        this.stepName = stepName;
        this.jobExecution = jobExecution;
    }

    public void apply(StepContribution contribution) {
        this.readCount += contribution.getReadCount();
        this.writeCount += contribution.getWriteCount();
        this.filterCount += contribution.getFilterCount();
        this.skipCount += contribution.getSkipCount();
        this.exitStatus = contribution.getExitStatus();
    }

    public void incrementCommitCount() { commitCount++; }
    public void incrementRollbackCount() { rollbackCount++; }

    public long getId() { return id; }
    public String getStepName() { return stepName; }
    public JobExecution getJobExecution() { return jobExecution; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public ExitStatus getExitStatus() { return exitStatus; }
    public void setExitStatus(ExitStatus exitStatus) { this.exitStatus = exitStatus; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public ExecutionContext getExecutionContext() { return executionContext; }
    public long getReadCount() { return readCount; }
    public long getWriteCount() { return writeCount; }
    public long getCommitCount() { return commitCount; }
    public long getRollbackCount() { return rollbackCount; }
    public long getFilterCount() { return filterCount; }
    public long getSkipCount() { return skipCount; }
}
