package com.hogwai.batch.core.runtime.launcher;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.runtime.JobExecution;

public interface JobLauncher {
    JobExecution run(Job job, JobParameters jobParameters) throws Exception;
}