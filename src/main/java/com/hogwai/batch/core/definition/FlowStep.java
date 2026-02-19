package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.StepExecution;

public class FlowStep implements Step {
    private final Flow flow;

    public FlowStep(Flow flow) { this.flow = flow; }

    @Override
    public String getName() { return flow.getName(); }

    @Override
    public void execute(StepExecution stepExecution) throws Exception {
        throw new UnsupportedOperationException("FlowStep must be executed by the launcher");
    }

    public Flow getFlow() { return flow; }
}
