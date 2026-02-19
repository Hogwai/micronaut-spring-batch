package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.ExitStatus;
import com.hogwai.batch.core.runtime.StepExecution;

import java.util.List;
import java.util.Map;

/**
 * Default {@link Flow} implementation that navigates steps using a transition map.
 * Each step's exit status is matched against transition patterns to determine the next step.
 */
public class SimpleFlow implements Flow {
    private final String name;
    private final Step startStep;
    private final Map<String, List<Transition>> transitions;

    /**
     * Creates a new simple flow.
     *
     * @param name        the flow name
     * @param startStep   the first step to execute
     * @param transitions map from step name to its list of possible transitions
     */
    public SimpleFlow(String name, Step startStep, Map<String, List<Transition>> transitions) {
        this.name = name;
        this.startStep = startStep;
        this.transitions = transitions;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() { return name; }

    /** {@inheritDoc} */
    @Override
    public ExitStatus execute(FlowExecutor executor) throws Exception {
        Step currentStep = startStep;
        ExitStatus lastExitStatus = ExitStatus.COMPLETED;

        while (currentStep != null) {
            StepExecution stepExecution;
            try {
                stepExecution = executor.executeStep(currentStep);
                lastExitStatus = stepExecution.getExitStatus();
            } catch (Exception e) {
                lastExitStatus = ExitStatus.FAILED;
            }

            currentStep = resolveNextStep(currentStep.getName(), lastExitStatus);
        }

        return lastExitStatus;
    }

    private Step resolveNextStep(String stepName, ExitStatus exitStatus) {
        List<Transition> stepTransitions = transitions.get(stepName);
        if (stepTransitions == null) return null;

        for (Transition t : stepTransitions) {
            if ("*".equals(t.pattern()) || t.pattern().equals(exitStatus.exitCode())) {
                return t.target();
            }
        }
        return null;
    }

    /**
     * Maps an exit status pattern to a target step. Use {@code "*"} as a wildcard pattern.
     *
     * @param pattern the exit code pattern to match (or {@code "*"} for any)
     * @param target  the step to transition to when matched
     */
    public record Transition(String pattern, Step target) {}
}
