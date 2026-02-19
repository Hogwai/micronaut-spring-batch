package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.ExitStatus;
import com.hogwai.batch.core.runtime.StepExecution;

/**
 * Defines conditional step execution with transitions based on exit status.
 * Allows building non-linear step graphs within a job.
 */
public interface Flow {
    /**
     * Returns the name of this flow.
     *
     * @return the flow name
     */
    String getName();

    /**
     * Executes the flow, using the provided executor to run individual steps
     * and following transitions based on their exit statuses.
     *
     * @param executor callback used to execute each step
     * @return the final exit status of the flow
     * @throws Exception if a step execution fails without a matching transition
     */
    ExitStatus execute(FlowExecutor executor) throws Exception;

    /**
     * Functional interface for executing a single step within a flow.
     */
    @FunctionalInterface
    interface FlowExecutor {
        /**
         * Executes the given step and returns its execution result.
         *
         * @param step the step to execute
         * @return the step execution result
         * @throws Exception if execution fails
         */
        StepExecution executeStep(Step step) throws Exception;
    }
}
