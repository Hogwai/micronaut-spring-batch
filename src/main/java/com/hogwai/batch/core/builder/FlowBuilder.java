package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Flow;
import com.hogwai.batch.core.definition.SimpleFlow;
import com.hogwai.batch.core.definition.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlowBuilder {
    private final String name;
    private Step startStep;
    private String currentStepName;
    private final Map<String, List<SimpleFlow.Transition>> transitions = new HashMap<>();

    public FlowBuilder(String name) { this.name = name; }

    public FlowBuilder start(Step step) {
        this.startStep = step;
        this.currentStepName = step.getName();
        return this;
    }

    public TransitionBuilder on(String pattern) {
        return new TransitionBuilder(this, currentStepName, pattern);
    }

    public FlowBuilder end() {
        return this;
    }

    public Flow build() {
        return new SimpleFlow(name, startStep, Map.copyOf(transitions));
    }

    private void addTransition(String fromStep, String pattern, Step target) {
        transitions.computeIfAbsent(fromStep, k -> new ArrayList<>())
                   .add(new SimpleFlow.Transition(pattern, target));
    }

    public static class TransitionBuilder {
        private final FlowBuilder flowBuilder;
        private final String fromStep;
        private final String pattern;

        TransitionBuilder(FlowBuilder flowBuilder, String fromStep, String pattern) {
            this.flowBuilder = flowBuilder;
            this.fromStep = fromStep;
            this.pattern = pattern;
        }

        public FlowBuilder to(Step step) {
            flowBuilder.addTransition(fromStep, pattern, step);
            return flowBuilder;
        }
    }
}
