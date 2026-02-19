package com.hogwai.batch.core.runtime.repository;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.JobInstance;
import com.hogwai.batch.core.runtime.StepExecution;

public interface JobRepository {
    JobInstance createJobInstance(String jobName, JobParameters jobParameters);
    JobExecution createJobExecution(JobInstance jobInstance, JobParameters jobParameters);
    StepExecution createStepExecution(JobExecution jobExecution, String stepName);
    void update(JobExecution execution);
    void update(StepExecution stepExecution);
}
