package com.hogwai.batch.core.listener;

import com.hogwai.batch.core.runtime.JobExecution;

public interface JobExecutionListener {
    default void beforeJob(JobExecution jobExecution) {}
    default void afterJob(JobExecution jobExecution) {}
}
