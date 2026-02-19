package com.hogwai.batch.core;

import com.hogwai.batch.core.runtime.ExecutionContext;
import com.hogwai.batch.core.runtime.StepContribution;

public interface Tasklet {
    RepeatStatus execute(StepContribution contribution, ExecutionContext context) throws Exception;
}
