package com.hogwai.batch.core.runtime.repository;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.JobInstance;
import com.hogwai.batch.core.runtime.StepExecution;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@Primary
public class InMemoryJobRepository implements JobRepository {
    private final AtomicLong instanceCounter = new AtomicLong(1);
    private final AtomicLong executionCounter = new AtomicLong(1);
    private final AtomicLong stepExecutionCounter = new AtomicLong(1);
    private final Map<Long, JobInstance> instances = new ConcurrentHashMap<>();
    private final Map<Long, JobExecution> executions = new ConcurrentHashMap<>();

    @Override
    public JobInstance createJobInstance(String jobName, JobParameters jobParameters) {
        JobInstance instance = new JobInstance(instanceCounter.getAndIncrement(), jobName);
        instances.put(instance.id(), instance);
        return instance;
    }

    @Override
    public JobExecution createJobExecution(JobInstance jobInstance, JobParameters jobParameters) {
        JobExecution exec = new JobExecution(executionCounter.getAndIncrement(), jobInstance, jobParameters);
        exec.setStartTime(Instant.now());
        exec.setStatus(BatchStatus.STARTED);
        executions.put(exec.getId(), exec);
        return exec;
    }

    @Override
    public StepExecution createStepExecution(JobExecution jobExecution, String stepName) {
        return jobExecution.createStepExecution(stepExecutionCounter.getAndIncrement(), stepName);
    }

    @Override
    public void update(JobExecution execution) {
        executions.put(execution.getId(), execution);
    }

    @Override
    public void update(StepExecution stepExecution) {
        // In-memory: already updated by reference
    }

    public JobExecution getJobExecution(Long executionId) {
        return executions.get(executionId);
    }
}
