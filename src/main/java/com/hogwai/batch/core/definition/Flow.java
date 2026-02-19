package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.ExitStatus;
import com.hogwai.batch.core.runtime.StepExecution;

public interface Flow {
    String getName();
    ExitStatus execute(FlowExecutor executor) throws Exception;

    @FunctionalInterface
    interface FlowExecutor {
        StepExecution executeStep(Step step) throws Exception;
    }
}
