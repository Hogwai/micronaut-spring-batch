package com.hogwai.batch.core.listener;

import com.hogwai.batch.core.runtime.StepExecution;

public interface StepExecutionListener {
    default void beforeStep(StepExecution stepExecution) {}
    default void afterStep(StepExecution stepExecution) {}
}
