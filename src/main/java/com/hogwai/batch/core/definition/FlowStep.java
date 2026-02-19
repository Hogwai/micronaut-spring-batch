package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.StepExecution;

/**
 * Adapter that wraps a {@link Flow} as a {@link Step}, allowing flows to be included
 * in a job's step list. Execution is handled by the job launcher, not by {@link #execute}.
 */
public class FlowStep implements Step {
    private final Flow flow;

    /**
     * Creates a new flow step wrapping the given flow.
     *
     * @param flow the flow to wrap
     */
    public FlowStep(Flow flow) { this.flow = flow; }

    /** {@inheritDoc} */
    @Override
    public String getName() { return flow.getName(); }

    /**
     * Not supported directly; the job launcher must detect {@code FlowStep}
     * and delegate to {@link #getFlow()} instead.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void execute(StepExecution stepExecution) throws Exception {
        throw new UnsupportedOperationException("FlowStep must be executed by the launcher");
    }

    /**
     * Returns the underlying flow.
     *
     * @return the wrapped flow
     */
    public Flow getFlow() { return flow; }
}
