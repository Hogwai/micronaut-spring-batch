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

/**
 * In-memory implementation of {@link JobRepository}, suitable for testing and simple use cases.
 * All data is stored in concurrent maps and lost when the application shuts down.
 */
@Singleton
@Primary
public class InMemoryJobRepository implements JobRepository {
    private final AtomicLong instanceCounter = new AtomicLong(1);
    private final AtomicLong executionCounter = new AtomicLong(1);
    private final AtomicLong stepExecutionCounter = new AtomicLong(1);
    private final Map<Long, JobInstance> instances = new ConcurrentHashMap<>();
    private final Map<Long, JobExecution> executions = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public JobInstance createJobInstance(String jobName, JobParameters jobParameters) {
        JobInstance instance = new JobInstance(instanceCounter.getAndIncrement(), jobName);
        instances.put(instance.id(), instance);
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public JobExecution createJobExecution(JobInstance jobInstance, JobParameters jobParameters) {
        JobExecution exec = new JobExecution(executionCounter.getAndIncrement(), jobInstance, jobParameters);
        exec.setStartTime(Instant.now());
        exec.setStatus(BatchStatus.STARTED);
        executions.put(exec.getId(), exec);
        return exec;
    }

    /** {@inheritDoc} */
    @Override
    public StepExecution createStepExecution(JobExecution jobExecution, String stepName) {
        return jobExecution.createStepExecution(stepExecutionCounter.getAndIncrement(), stepName);
    }

    /** {@inheritDoc} */
    @Override
    public void update(JobExecution execution) {
        executions.put(execution.getId(), execution);
    }

    /** {@inheritDoc} */
    @Override
    public void update(StepExecution stepExecution) {
        // In-memory: already updated by reference
    }

    /**
     * Retrieves a job execution by its identifier.
     *
     * @param executionId the execution identifier
     * @return the job execution, or {@code null} if not found
     */
    public JobExecution getJobExecution(Long executionId) {
        return executions.get(executionId);
    }
}
