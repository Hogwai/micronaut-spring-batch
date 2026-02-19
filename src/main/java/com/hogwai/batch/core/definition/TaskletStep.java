package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.RepeatStatus;
import com.hogwai.batch.core.Tasklet;
import com.hogwai.batch.core.listener.StepExecutionListener;
import com.hogwai.batch.core.runtime.StepContribution;
import com.hogwai.batch.core.runtime.StepExecution;

import java.util.List;

/**
 * A {@link Step} that delegates execution to a {@link Tasklet}, calling it repeatedly
 * until it returns {@link RepeatStatus#FINISHED}.
 */
public class TaskletStep implements Step {
    private final String name;
    private final Tasklet tasklet;
    private final List<StepExecutionListener> listeners;

    /**
     * Creates a new tasklet step.
     *
     * @param name      the step name
     * @param tasklet   the tasklet to execute
     * @param listeners step-level lifecycle listeners
     */
    public TaskletStep(String name, Tasklet tasklet, List<StepExecutionListener> listeners) {
        this.name = name;
        this.tasklet = tasklet;
        this.listeners = listeners;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() { return name; }

    /** {@inheritDoc} */
    @Override
    public void execute(StepExecution stepExecution) throws Exception {
        listeners.forEach(l -> l.beforeStep(stepExecution));

        StepContribution contribution = new StepContribution();
        RepeatStatus status;
        do {
            status = tasklet.execute(contribution, stepExecution.getExecutionContext());
        } while (status == RepeatStatus.CONTINUABLE);

        stepExecution.apply(contribution);
        listeners.forEach(l -> l.afterStep(stepExecution));
    }
}
