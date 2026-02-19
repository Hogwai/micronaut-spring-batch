package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Flow;
import com.hogwai.batch.core.definition.SimpleFlow;
import com.hogwai.batch.core.definition.Step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for constructing {@link Flow} instances with conditional transitions between steps.
 *
 * @see Flow
 * @see SimpleFlow
 */
public class FlowBuilder {
    private final String name;
    private Step startStep;
    private String currentStepName;
    private final Map<String, List<SimpleFlow.Transition>> transitions = new HashMap<>();

    /**
     * Creates a new flow builder with the given flow name.
     *
     * @param name the unique name for the flow
     */
    public FlowBuilder(String name) { this.name = name; }

    /**
     * Sets the initial step of the flow.
     *
     * @param step the first step to execute in the flow
     * @return this builder
     */
    public FlowBuilder start(Step step) {
        this.startStep = step;
        this.currentStepName = step.getName();
        return this;
    }

    /**
     * Begins defining a conditional transition from the current step based on an exit status pattern.
     *
     * @param pattern the exit status pattern to match (e.g., "COMPLETED", "FAILED", "*")
     * @return a {@link TransitionBuilder} to specify the target step
     */
    public TransitionBuilder on(String pattern) {
        return new TransitionBuilder(this, currentStepName, pattern);
    }

    /**
     * Terminates the current flow definition chain. Called after all transitions are defined.
     *
     * @return this builder
     */
    public FlowBuilder end() {
        return this;
    }

    /**
     * Builds and returns an immutable {@link Flow} instance from the configured steps and transitions.
     *
     * @return the constructed flow
     */
    public Flow build() {
        return new SimpleFlow(name, startStep, Map.copyOf(transitions));
    }

    private void addTransition(String fromStep, String pattern, Step target) {
        transitions.computeIfAbsent(fromStep, k -> new ArrayList<>())
                   .add(new SimpleFlow.Transition(pattern, target));
    }

    /**
     * Intermediate builder for specifying the target step of a conditional transition.
     */
    public static class TransitionBuilder {
        private final FlowBuilder flowBuilder;
        private final String fromStep;
        private final String pattern;

        TransitionBuilder(FlowBuilder flowBuilder, String fromStep, String pattern) {
            this.flowBuilder = flowBuilder;
            this.fromStep = fromStep;
            this.pattern = pattern;
        }

        /**
         * Sets the target step for this transition and returns to the flow builder.
         *
         * @param step the step to transition to when the pattern matches
         * @return the parent {@link FlowBuilder} for continued configuration
         */
        public FlowBuilder to(Step step) {
            flowBuilder.addTransition(fromStep, pattern, step);
            return flowBuilder;
        }
    }
}
