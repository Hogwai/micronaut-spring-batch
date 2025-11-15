package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;

import java.util.ArrayList;
import java.util.List;

public class JobBuilder {
    private final String name;
    private final List<Step> steps = new ArrayList<>();

    public JobBuilder(String name) { this.name = name; }

    public JobBuilder start(Step step) {
        steps.add(step);
        return this;
    }

    public JobBuilder next(Step step) {
        steps.add(step);
        return this;
    }

    public Job build() {
        return new DefaultJob(name, List.copyOf(steps));
    }

    private static class DefaultJob implements Job {
        private final String name;
        private final List<Step> steps;

        DefaultJob(String name, List<Step> steps) {
            this.name = name;
            this.steps = steps;
        }

        @Override public String getName() { return name; }
        @Override public List<Step> getSteps() { return steps; }
    }
}