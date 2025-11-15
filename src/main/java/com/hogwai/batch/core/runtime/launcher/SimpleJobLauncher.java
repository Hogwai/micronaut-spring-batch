package com.hogwai.batch.core.runtime.launcher;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.repository.JobRepository;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.time.Instant;

@Singleton
@Primary
@Requires(missingBeans = JobLauncher.class)
public class SimpleJobLauncher implements JobLauncher {

    private final JobRepository jobRepository;

    public SimpleJobLauncher(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public JobExecution run(Job job, JobParameters jobParameters) throws Exception {
        JobExecution execution = jobRepository.createJobExecution(job.getName(), jobParameters);
        execution.setStatus(BatchStatus.STARTED);

        try {
            for (Step step : job.getSteps()) {
                executeStep(step, execution);
            }
            execution.setStatus(BatchStatus.COMPLETED);
        } catch (Exception e) {
            execution.setStatus(BatchStatus.FAILED);
            throw e;
        } finally {
            execution.setEndTime(Instant.now());
            jobRepository.update(execution);
        }
        return execution;
    }

    private void executeStep(Step step, JobExecution jobExec) throws Exception {
        step.execute();
    }
}