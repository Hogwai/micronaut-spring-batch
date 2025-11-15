package com.hogwai.example.simple.config;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.batch.core.ItemReader;
import com.hogwai.batch.core.ItemWriter;
import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.example.simple.model.Person;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
public class JobConfig {

    @Bean
    @Singleton
    @Named("importJob")
    public Job importJob(Step importStep) {
        return new JobBuilder("importJob")
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