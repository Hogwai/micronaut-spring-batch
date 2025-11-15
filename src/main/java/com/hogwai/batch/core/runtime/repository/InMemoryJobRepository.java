package com.hogwai.batch.core.runtime.repository;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@Primary
@Requires(missingBeans = JobRepository.class)
public class InMemoryJobRepository implements JobRepository {
    private final AtomicLong counter = new AtomicLong(1);
    private final Map<Long, JobExecution> executions = new ConcurrentHashMap<>();

    @Override
    public JobExecution createJobExecution(String jobName, JobParameters jobParameters) {
        JobExecution exec = new JobExecution(counter.getAndIncrement(), jobParameters);
        exec.setStartTime(Instant.now());
        exec.setStatus(BatchStatus.STARTED);
        executions.put(exec.getId(), exec);
        return exec;
    }

    @Override
    public void update(JobExecution execution) {
        executions.put(execution.getId(), execution);
    }

    public JobExecution getJobExecution(Long executionId) {
        Objects.requireNonNull(executionId);
        return executions.get(executionId);
    }
}
