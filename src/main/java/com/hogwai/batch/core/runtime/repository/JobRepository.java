package com.hogwai.batch.core.runtime.repository;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.JobExecution;

public interface JobRepository {
    JobExecution createJobExecution(String jobName, JobParameters jobParameters);
    void update(JobExecution execution);
}