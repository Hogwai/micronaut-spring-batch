package com.hogwai.batch.engine;

import com.hogwai.batch.core.definition.Job;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Primary
@Requires(property = "micronaut.batch.engine", value = "default")
public class DefaultBatchEngine implements BatchEngine {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultBatchEngine.class);

    @Override
    public void launchJob(Job job) {

    }
}
