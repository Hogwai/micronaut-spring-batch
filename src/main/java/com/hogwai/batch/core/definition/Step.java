package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.StepExecution;

public interface Step {
    String getName();
    void execute(StepExecution stepExecution) throws Exception;
}
