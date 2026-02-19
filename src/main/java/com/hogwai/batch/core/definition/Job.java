package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.listener.JobExecutionListener;

import java.util.List;

public interface Job {
    String getName();
    List<Step> getSteps(); // ou: Step getStep(String name)
    default List<JobExecutionListener> getListeners() { return List.of(); }
}
