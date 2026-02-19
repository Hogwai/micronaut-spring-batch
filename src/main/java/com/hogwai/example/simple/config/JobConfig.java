package com.hogwai.example.simple.config;

import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.listener.JobExecutionListener;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.example.simple.model.Person;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class JobConfig {

    private static final Logger LOG = LoggerFactory.getLogger(JobConfig.class);

    @Bean
    @Singleton
    @Named("importJob")
    public Job importJob(Step importStep) {
        return new JobBuilder("importJob")
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution exec) {
                        LOG.info("Starting job: {}", exec.getJobInstance().jobName());
                    }
                    @Override
                    public void afterJob(JobExecution exec) {
                        LOG.info("Job {} finished with status {}", exec.getJobInstance().jobName(), exec.getStatus());
                    }
                })
                .start(importStep)
                .build();
    }

    @Bean
    @Singleton
    public Step importStep(
            CsvPersonReader reader,
            UppercaseProcessor processor,
            ConsoleWriter writer
    ) {
        return new StepBuilder("importStep")
                .<Person, Person>chunk(2)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
