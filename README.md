# Micronaut Spring Batch

A lightweight Spring Batch-like framework built on top of Micronaut, providing chunk-oriented processing, tasklet steps, fault tolerance, conditional flows, and comprehensive lifecycle listeners.

## Overview

Micronaut Spring Batch brings the familiar programming model of Spring Batch to the Micronaut ecosystem. It provides a fluent builder API to define batch jobs composed of steps that read, process, and write data in configurable chunks. The framework supports fault-tolerant processing with skip and retry policies, conditional step flows based on exit status, and fine-grained lifecycle listeners at every level of execution.

**Key concepts:**

| Concept    | Description                                                                 |
|------------|-----------------------------------------------------------------------------|
| **Job**    | A batch process composed of one or more steps, executed sequentially.       |
| **Step**   | The smallest independent unit of execution within a job.                    |
| **Chunk**  | A fixed-size group of items that are read, processed, and written together. |
| **Tasklet**| A callback for executing a single unit of work (non-chunk-oriented).        |
| **Flow**   | A directed graph of steps with conditional transitions based on exit status.|

## Features

- Chunk-oriented processing (read-process-write cycle with configurable chunk size)
- Tasklet steps for arbitrary single-operation work
- Fault tolerance with configurable skip policies, retry policies, and backoff strategies
- Conditional flow execution with transitions based on step exit status
- Job and step execution tracking (JobExecution, StepExecution, metrics)
- Comprehensive listener support (job, step, chunk, item-read, item-process, item-write, skip)
- Fluent builder API for constructing jobs, steps, and flows
- In-memory job repository for execution metadata
- Native Micronaut dependency injection integration

## Quick Start

```java
import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.runtime.launcher.SimpleJobLauncher;
import com.hogwai.batch.core.runtime.repository.InMemoryJobRepository;

import java.util.Iterator;
import java.util.List;

public class QuickStart {

    public static void main(String[] args) throws Exception {
        // Data source
        Iterator<String> data = List.of("alice", "bob", "charlie").iterator();

        // Build a step
        Step step = new StepBuilder("greetStep")
                .<String, String>chunk(2)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(name -> "Hello, " + name + "!")
                .writer(items -> items.forEach(System.out::println))
                .build();

        // Build a job
        Job job = new JobBuilder("greetJob")
                .start(step)
                .build();

        // Launch the job
        SimpleJobLauncher launcher = new SimpleJobLauncher(new InMemoryJobRepository());
        JobParameters params = JobParameters.builder()
                .addString("mode", "demo")
                .toJobParameters();

        launcher.run(job, params);
    }
}
```

## Architecture

```
com.hogwai.batch.core
├── ItemReader              -- reads items one at a time from a data source
├── ItemProcessor           -- transforms an input item into an output item
├── ItemWriter              -- writes a chunk of items to an output destination
├── Tasklet                 -- executes a single unit of work, returns RepeatStatus
├── RepeatStatus            -- CONTINUABLE or FINISHED
│
├── builder/
│   ├── JobBuilder          -- fluent builder for Job instances
│   ├── StepBuilder         -- fluent builder for chunk-oriented and tasklet steps
│   └── FlowBuilder         -- fluent builder for conditional step flows
│
├── config/
│   └── JobParameters       -- immutable map of typed parameters passed to a job run
│
├── definition/
│   ├── Job                 -- interface: getName(), getSteps(), getListeners()
│   ├── Step                -- interface: getName(), execute(StepExecution)
│   ├── ChunkOrientedStep   -- reads/processes/writes items in fixed-size chunks
│   ├── FaultTolerantChunkStep -- chunk step with skip, retry, and backoff support
│   ├── TaskletStep         -- executes a Tasklet until FINISHED
│   ├── Flow                -- interface for conditional step execution
│   ├── SimpleFlow          -- flow implementation with pattern-matched transitions
│   └── FlowStep            -- adapter that wraps a Flow as a Step
│
├── listener/
│   ├── JobExecutionListener   -- beforeJob / afterJob
│   ├── StepExecutionListener  -- beforeStep / afterStep
│   ├── ChunkListener          -- beforeChunk / afterChunk / afterChunkError
│   ├── ItemReadListener       -- beforeRead / afterRead / onReadError
│   ├── ItemProcessListener    -- beforeProcess / afterProcess / onProcessError
│   ├── ItemWriteListener      -- beforeWrite / afterWrite / onWriteError
│   └── SkipListener           -- onSkipInProcess
│
├── policy/
│   ├── SkipPolicy / SimpleSkipPolicy       -- decides whether to skip a failed item
│   ├── RetryPolicy / SimpleRetryPolicy     -- decides whether to retry a failed operation
│   └── BackoffPolicy / FixedBackoffPolicy  -- delays between retry attempts
│
└── runtime/
    ├── BatchStatus         -- STARTED, COMPLETED, FAILED, etc.
    ├── ExitStatus          -- exit code returned by a step or job
    ├── ExecutionContext     -- shared key-value store for passing data
    ├── JobInstance          -- logical job identity (name + parameters)
    ├── JobExecution         -- runtime state of a single job run
    ├── StepExecution        -- runtime state of a single step run
    ├── StepContribution     -- mutable counters for read/write/filter/skip metrics
    ├── launcher/
    │   ├── JobLauncher          -- interface for launching jobs
    │   └── SimpleJobLauncher    -- default launcher implementation (@Singleton)
    └── repository/
        ├── JobRepository            -- interface for persisting execution metadata
        └── InMemoryJobRepository    -- in-memory implementation (@Singleton)
```

## Usage Guide

### Chunk-Oriented Step

A chunk-oriented step reads items one at a time, optionally processes each item, and writes them in groups of a configurable chunk size.

```java
Step step = new StepBuilder("importStep")
        .<Person, Person>chunk(100)
        .reader(personReader)
        .processor(validationProcessor)
        .writer(databaseWriter)
        .build();
```

The `reader` must return `null` when input is exhausted. The `processor` may return `null` to filter out an item. The `writer` receives a `List<O>` of up to `chunkSize` items.

### Tasklet Step

A tasklet step executes a `Tasklet` callback repeatedly until it returns `RepeatStatus.FINISHED`.

```java
Step step = new StepBuilder("cleanup")
        .tasklet((contribution, context) -> {
            // perform cleanup logic
            return RepeatStatus.FINISHED;
        })
        .build();
```

The `StepContribution` parameter allows tracking custom metrics, and the `ExecutionContext` can be used to share state between invocations when returning `RepeatStatus.CONTINUABLE`.

### Fault Tolerance (Skip and Retry)

Enable fault tolerance on a chunk step to handle transient and expected failures gracefully.

```java
SimpleSkipPolicy skipPolicy = new SimpleSkipPolicy(10);
skipPolicy.registerSkippableException(ValidationException.class);

SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
retryPolicy.registerRetryableException(IOException.class);

Step step = new StepBuilder("resilientStep")
        .<String, String>chunk(50)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .faultTolerant()
        .skipPolicy(skipPolicy)
        .retryPolicy(retryPolicy)
        .backoffPolicy(new FixedBackoffPolicy(1000))
        .build();
```

- **SkipPolicy** -- controls how many items can be skipped and which exceptions are skippable.
- **RetryPolicy** -- controls how many retry attempts are made and which exceptions are retryable.
- **BackoffPolicy** -- introduces a delay between retry attempts (e.g., fixed interval in milliseconds).

### Conditional Flow

Use `FlowBuilder` to define conditional transitions between steps based on their exit status.

```java
Step validateStep = new StepBuilder("validate")
        .tasklet((contribution, context) -> {
            // validation logic
            return RepeatStatus.FINISHED;
        })
        .build();

Step processStep = new StepBuilder("process")
        .<String, String>chunk(100)
        .reader(reader).processor(processor).writer(writer)
        .build();

Step errorHandlerStep = new StepBuilder("errorHandler")
        .tasklet((contribution, context) -> {
            // error handling logic
            return RepeatStatus.FINISHED;
        })
        .build();

Flow flow = new FlowBuilder("mainFlow")
        .start(validateStep)
        .on("COMPLETED").to(processStep)
        .on("FAILED").to(errorHandlerStep)
        .end()
        .build();

Job job = new JobBuilder("flowJob")
        .start(flow)
        .build();
```

Flows can be mixed with regular steps in a job:

```java
Job job = new JobBuilder("mixedJob")
        .start(initStep)
        .next(flow)
        .next(finalizeStep)
        .build();
```

### Listeners

The framework provides listeners at every level of the batch execution lifecycle.

**Job-level listener:**

```java
Job job = new JobBuilder("observedJob")
        .listener(new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                System.out.println("Job starting: " + jobExecution.getJobInstance().jobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                System.out.println("Job finished with status: " + jobExecution.getStatus());
            }
        })
        .start(step)
        .build();
```

**Step-level listener:**

```java
Step step = new StepBuilder("monitoredStep")
        .<String, String>chunk(50)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .listener(new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                System.out.println("Step starting: " + stepExecution.getStepName());
            }

            @Override
            public void afterStep(StepExecution stepExecution) {
                System.out.println("Step completed. Read count: " + stepExecution.getReadCount());
            }
        })
        .build();
```

**Chunk and item-level listeners:**

```java
Step step = new StepBuilder("fullyMonitoredStep")
        .<String, String>chunk(10)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .chunkListener(new ChunkListener() {
            @Override
            public void beforeChunk() { /* ... */ }

            @Override
            public void afterChunk() { /* ... */ }

            @Override
            public void afterChunkError(Exception e) { /* ... */ }
        })
        .itemReadListener(new ItemReadListener<>() {
            @Override
            public void afterRead(String item) {
                System.out.println("Read: " + item);
            }
        })
        .build();
```

**Skip listener** (available on fault-tolerant steps):

```java
Step step = new StepBuilder("skippableStep")
        .<String, String>chunk(50)
        .reader(reader).processor(processor).writer(writer)
        .faultTolerant()
        .skipPolicy(new SimpleSkipPolicy(5))
        .skipListener(new SkipListener<>() {
            @Override
            public void onSkipInProcess(String item, Throwable t) {
                System.err.println("Skipped item: " + item + " due to: " + t.getMessage());
            }
        })
        .build();
```

### Job Parameters

Pass typed parameters to a job run using the builder API.

```java
JobParameters params = JobParameters.builder()
        .addString("inputFile", "/data/input.csv")
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters();

JobExecution execution = jobLauncher.run(job, params);
```

Parameters are accessible via `JobParameters.getString(key)` and `JobParameters.getLong(key)`.

## Micronaut Integration

The framework integrates natively with Micronaut's dependency injection. Use `@Factory` to define your job configuration, `@Singleton` for shared components, and `@Named` to distinguish between multiple beans of the same type.

```java
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
```

Use `@EventListener` on a Micronaut `StartupEvent` to trigger job execution when the application starts:

```java
@Singleton
public class BatchRunner {

    private final JobLauncher jobLauncher;
    private final Job importJob;

    public BatchRunner(JobLauncher jobLauncher, @Named("importJob") Job importJob) {
        this.jobLauncher = jobLauncher;
        this.importJob = importJob;
    }

    @EventListener
    public void onStartup(StartupEvent event) {
        JobParameters params = JobParameters.builder()
                .addString("source", "persons.csv")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution exec = jobLauncher.run(importJob, params);
            System.out.println("Job finished: " + exec.getStatus());
        } catch (Exception e) {
            throw new RuntimeException("Job failed", e);
        }
    }
}
```

The `SimpleJobLauncher` and `InMemoryJobRepository` are annotated with `@Singleton` and will be automatically discovered by Micronaut's bean context. See the example application under `com.hogwai.example.simple` for a complete working setup.

## Project Structure

```
src/main/java/com/hogwai/
├── Application.java                          -- Micronaut application entry point
├── batch/core/
│   ├── ItemReader.java                       -- core read interface
│   ├── ItemProcessor.java                    -- core process interface
│   ├── ItemWriter.java                       -- core write interface
│   ├── Tasklet.java                          -- tasklet callback interface
│   ├── RepeatStatus.java                     -- CONTINUABLE / FINISHED enum
│   ├── builder/
│   │   ├── JobBuilder.java
│   │   ├── StepBuilder.java
│   │   └── FlowBuilder.java
│   ├── config/
│   │   └── JobParameters.java
│   ├── definition/
│   │   ├── Job.java
│   │   ├── Step.java
│   │   ├── ChunkOrientedStep.java
│   │   ├── FaultTolerantChunkStep.java
│   │   ├── TaskletStep.java
│   │   ├── Flow.java
│   │   ├── SimpleFlow.java
│   │   └── FlowStep.java
│   ├── listener/
│   │   ├── JobExecutionListener.java
│   │   ├── StepExecutionListener.java
│   │   ├── ChunkListener.java
│   │   ├── ItemReadListener.java
│   │   ├── ItemProcessListener.java
│   │   ├── ItemWriteListener.java
│   │   └── SkipListener.java
│   ├── policy/
│   │   ├── SkipPolicy.java
│   │   ├── SimpleSkipPolicy.java
│   │   ├── RetryPolicy.java
│   │   ├── SimpleRetryPolicy.java
│   │   ├── BackoffPolicy.java
│   │   └── FixedBackoffPolicy.java
│   └── runtime/
│       ├── BatchStatus.java
│       ├── ExitStatus.java
│       ├── ExecutionContext.java
│       ├── JobInstance.java
│       ├── JobExecution.java
│       ├── StepExecution.java
│       ├── StepContribution.java
│       ├── launcher/
│       │   ├── JobLauncher.java
│       │   └── SimpleJobLauncher.java
│       └── repository/
│           ├── JobRepository.java
│           └── InMemoryJobRepository.java
└── example/simple/
    ├── BatchRunner.java                      -- startup event listener
    ├── config/
    │   ├── JobConfig.java                    -- job and step bean definitions
    │   ├── CsvPersonReader.java              -- ItemReader for CSV files
    │   ├── UppercaseProcessor.java           -- ItemProcessor example
    │   └── ConsoleWriter.java                -- ItemWriter to stdout
    └── model/
        └── Person.java
```

## Requirements

| Dependency    | Version   |
|---------------|-----------|
| Java          | 21        |
| Micronaut     | 4.x       |
| Gradle        | 8.x       |

## License

This project is licensed under the [MIT License](LICENSE).
