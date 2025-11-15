package com.hogwai.batch.core.definition;

import java.util.List;

public interface Job {
    String getName();
    List<Step> getSteps(); // ou: Step getStep(String name)
}