package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Flow;
import com.hogwai.batch.core.definition.FlowStep;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.listener.JobExecutionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing {@link Job} instances from a sequence of steps and/or flows.
 *
 * @see Job
 * @see StepBuilder
 * @see FlowBuilder
 */
public class JobBuilder {
    private final String name;
    private final List<Step> steps = new ArrayList<>();
    private final List<JobExecutionListener> listeners = new ArrayList<>();

    /**
     * Creates a new job builder with the given job name.
     *
     * @param name the unique name for the job
     */
    public JobBuilder(String name) { this.name = name; }

    /**
     * Sets the first step of the job.
     *
     * @param step the initial step to execute
     * @return this builder
     */
    public JobBuilder start(Step step) { steps.add(step); return this; }

    /**
     * Appends a step to the job's execution sequence.
     *
     * @param step the next step to execute
     * @return this builder
     */
    public JobBuilder next(Step step) { steps.add(step); return this; }

    /**
     * Sets the first step of the job to a flow, wrapped as a {@link FlowStep}.
     *
     * @param flow the initial flow to execute
     * @return this builder
     */
    public JobBuilder start(Flow flow) { steps.add(new FlowStep(flow)); return this; }

    /**
     * Appends a flow (wrapped as a {@link FlowStep}) to the job's execution sequence.
     *
     * @param flow the next flow to execute
     * @return this builder
     */
    public JobBuilder next(Flow flow) { steps.add(new FlowStep(flow)); return this; }

    /**
     * Registers a listener to be notified of job-level lifecycle events.
     *
     * @param listener the job execution listener
     * @return this builder
     */
    public JobBuilder listener(JobExecutionListener listener) { listeners.add(listener); return this; }

    /**
     * Builds and returns an immutable {@link Job} instance from the configured steps and listeners.
     *
     * @return the constructed job
     */
    public Job build() {
        return new DefaultJob(name, List.copyOf(steps), List.copyOf(listeners));
    }

    /**
     * Default immutable implementation of {@link Job}.
     */
    private static class DefaultJob implements Job {
        private final String name;
        private final List<Step> steps;
        private final List<JobExecutionListener> listeners;

        DefaultJob(String name, List<Step> steps, List<JobExecutionListener> listeners) {
            this.name = name;
            this.steps = steps;
            this.listeners = listeners;
        }

        @Override public String getName() { return name; }
        @Override public List<Step> getSteps() { return steps; }
        @Override public List<JobExecutionListener> getListeners() { return listeners; }
    }
}
