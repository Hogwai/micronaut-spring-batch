package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.ExitStatus;
import com.hogwai.batch.core.runtime.StepExecution;

import java.util.List;
import java.util.Map;

public class SimpleFlow implements Flow {
    private final String name;
    private final Step startStep;
    private final Map<String, List<Transition>> transitions;

    public SimpleFlow(String name, Step startStep, Map<String, List<Transition>> transitions) {
        this.name = name;
        this.startStep = startStep;
        this.transitions = transitions;
    }

    @Override
    public String getName() { return name; }

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

    public record Transition(String pattern, Step target) {}
}
