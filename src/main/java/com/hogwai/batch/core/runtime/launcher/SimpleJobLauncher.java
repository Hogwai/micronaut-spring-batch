package com.hogwai.batch.core.runtime.launcher;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Flow;
import com.hogwai.batch.core.definition.FlowStep;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.ExitStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.JobInstance;
import com.hogwai.batch.core.runtime.StepExecution;
import com.hogwai.batch.core.runtime.repository.JobRepository;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Default {@link JobLauncher} implementation that executes steps sequentially
 * and manages the job execution lifecycle via a {@link JobRepository}.
 */
@Singleton
@Primary
public class SimpleJobLauncher implements JobLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleJobLauncher.class);
    private final JobRepository jobRepository;

    /**
     * Creates a new launcher backed by the given repository.
     *
     * @param jobRepository the repository for persisting execution metadata
     */
    public SimpleJobLauncher(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /** {@inheritDoc} */
    @Override
    public JobExecution run(Job job, JobParameters jobParameters) throws Exception {
        JobInstance jobInstance = jobRepository.createJobInstance(job.getName(), jobParameters);
        JobExecution execution = jobRepository.createJobExecution(jobInstance, jobParameters);
        execution.setStatus(BatchStatus.STARTED);

        try {
            job.getListeners().forEach(l -> l.beforeJob(execution));

            for (Step step : job.getSteps()) {
                executeStep(step, execution);
            }
            execution.setStatus(BatchStatus.COMPLETED);
            execution.setExitStatus(ExitStatus.COMPLETED);
        } catch (Exception e) {
            execution.setStatus(BatchStatus.FAILED);
            execution.setExitStatus(ExitStatus.FAILED);
            throw e;
        } finally {
            job.getListeners().forEach(l -> l.afterJob(execution));
            execution.setEndTime(Instant.now());
            jobRepository.update(execution);
        }
        return execution;
    }

    private void executeStep(Step step, JobExecution jobExecution) throws Exception {
        if (step instanceof FlowStep flowStep) {
            executeFlow(flowStep.getFlow(), jobExecution);
            return;
        }
        StepExecution stepExecution = jobRepository.createStepExecution(jobExecution, step.getName());
        stepExecution.setStartTime(Instant.now());
        stepExecution.setStatus(BatchStatus.STARTED);

        try {
            LOG.info("Executing step: {}", step.getName());
            step.execute(stepExecution);
            stepExecution.setStatus(BatchStatus.COMPLETED);
            stepExecution.setExitStatus(ExitStatus.COMPLETED);
        } catch (Exception e) {
            stepExecution.setStatus(BatchStatus.FAILED);
            stepExecution.setExitStatus(ExitStatus.FAILED);
            throw e;
        } finally {
            stepExecution.setEndTime(Instant.now());
            jobRepository.update(stepExecution);
        }
    }

    private void executeFlow(Flow flow, JobExecution jobExecution) throws Exception {
        flow.execute(step -> {
            StepExecution stepExecution = jobRepository.createStepExecution(jobExecution, step.getName());
            stepExecution.setStartTime(Instant.now());
            stepExecution.setStatus(BatchStatus.STARTED);
            try {
                LOG.info("Executing flow step: {}", step.getName());
                step.execute(stepExecution);
                stepExecution.setStatus(BatchStatus.COMPLETED);
                stepExecution.setExitStatus(ExitStatus.COMPLETED);
            } catch (Exception e) {
                stepExecution.setStatus(BatchStatus.FAILED);
                stepExecution.setExitStatus(ExitStatus.FAILED);
            } finally {
                stepExecution.setEndTime(Instant.now());
                jobRepository.update(stepExecution);
            }
            return stepExecution;
        });
    }
}
