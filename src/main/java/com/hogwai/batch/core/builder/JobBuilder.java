package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Flow;
import com.hogwai.batch.core.definition.FlowStep;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.listener.JobExecutionListener;

import java.util.ArrayList;
import java.util.List;

public class JobBuilder {
    private final String name;
    private final List<Step> steps = new ArrayList<>();
    private final List<JobExecutionListener> listeners = new ArrayList<>();

    public JobBuilder(String name) { this.name = name; }

    public JobBuilder start(Step step) { steps.add(step); return this; }
    public JobBuilder next(Step step) { steps.add(step); return this; }
    public JobBuilder start(Flow flow) { steps.add(new FlowStep(flow)); return this; }
    public JobBuilder next(Flow flow) { steps.add(new FlowStep(flow)); return this; }
    public JobBuilder listener(JobExecutionListener listener) { listeners.add(listener); return this; }

    public Job build() {
        return new DefaultJob(name, List.copyOf(steps), List.copyOf(listeners));
    }

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
