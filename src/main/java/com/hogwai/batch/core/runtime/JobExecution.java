package com.hogwai.batch.core.runtime;

import com.hogwai.batch.core.config.JobParameters;

import java.time.Instant;

public class JobExecution {
    private final long id;
    private final JobParameters jobParameters;
    private volatile BatchStatus status = BatchStatus.STARTING;
    private volatile Instant startTime;
    private volatile Instant endTime;

    public JobExecution(long id, JobParameters jobParameters) {
        this.id = id;
        this.jobParameters = jobParameters;
    }

    public long getId() { return id; }
    public JobParameters getJobParameters() { return jobParameters; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
}