package com.hogwai.batch.core;

import com.hogwai.batch.core.runtime.ExecutionContext;
import com.hogwai.batch.core.runtime.StepContribution;

/**
 * Callback interface for executing a single unit of work within a step.
 * Returns a {@link RepeatStatus} to indicate whether execution should continue.
 *
 * @see RepeatStatus
 */
public interface Tasklet {
    /**
     * Executes the tasklet logic.
     *
     * @param contribution mutable context for tracking read/write/skip counts
     * @param context      shared execution context for passing data between invocations
     * @return {@link RepeatStatus#CONTINUABLE} to be called again, or
     *         {@link RepeatStatus#FINISHED} when done
     * @throws Exception if an unrecoverable error occurs
     */
    RepeatStatus execute(StepContribution contribution, ExecutionContext context) throws Exception;
}
