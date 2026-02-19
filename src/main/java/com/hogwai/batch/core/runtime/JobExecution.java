package com.hogwai.batch.core.runtime;

import com.hogwai.batch.core.config.JobParameters;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public JobExecution(long id, JobInstance jobInstance, JobParameters jobParameters) {
        this.id = id;
        this.jobInstance = jobInstance;
        this.jobParameters = jobParameters;
    }

    public StepExecution createStepExecution(long stepExecId, String stepName) {
        StepExecution stepExecution = new StepExecution(stepExecId, stepName, this);
        stepExecutions.add(stepExecution);
        return stepExecution;
    }

    public long getId() { return id; }
    public JobInstance getJobInstance() { return jobInstance; }
    public JobParameters getJobParameters() { return jobParameters; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public ExitStatus getExitStatus() { return exitStatus; }
    public void setExitStatus(ExitStatus exitStatus) { this.exitStatus = exitStatus; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public ExecutionContext getExecutionContext() { return executionContext; }
    public List<StepExecution> getStepExecutions() { return Collections.unmodifiableList(stepExecutions); }
}
