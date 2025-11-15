package com.hogwai.batch.engine;

import com.hogwai.batch.core.definition.Job;

public interface BatchEngine {
    void launchJob(Job job);
}
