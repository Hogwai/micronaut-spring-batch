package com.hogwai.example.simple;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.launcher.JobLauncher;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BatchRunner {

    private static final Logger LOG = LoggerFactory.getLogger(BatchRunner.class);


    private final JobLauncher jobLauncher;
    private final Job importJob;
    private final ApplicationContext ctx;

    public BatchRunner(JobLauncher jobLauncher,
                       @Named("importJob") Job importJob,
                       ApplicationContext ctx) {
        this.jobLauncher = jobLauncher;
        this.importJob = importJob;
        this.ctx = ctx;
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        JobParameters params = JobParameters.builder()
                                            .addString("source", "mock-csv")
                                            .addLong("run.id", System.currentTimeMillis())
                                            .toJobParameters();

        try {
            JobExecution exec = jobLauncher.run(importJob, params);

            LOG.info("Job {} ended with status {}", importJob.getName(), exec.getStatus());
        } catch (Exception e) {
            String msg = "Error while running job %s: ".formatted(importJob.getName());
            LOG.error(msg, e);
            throw new RuntimeException(e);
        } finally {
            ctx.close();
        }
    }
}
