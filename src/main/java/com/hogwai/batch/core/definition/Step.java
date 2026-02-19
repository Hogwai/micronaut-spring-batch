package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.StepExecution;

/**
 * Represents a single step within a batch job, the smallest independent unit of execution.
 */
public interface Step {
    /**
     * Returns the name of this step.
     *
     * @return the step name
     */
    String getName();

    /**
     * Executes this step's logic within the given step execution context.
     *
     * @param stepExecution the runtime context tracking this step's execution state
     * @throws Exception if an error occurs during execution
     */
    void execute(StepExecution stepExecution) throws Exception;
}
