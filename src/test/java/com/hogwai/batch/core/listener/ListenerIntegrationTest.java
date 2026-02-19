package com.hogwai.batch.core.listener;

import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.StepExecution;
import com.hogwai.batch.core.runtime.launcher.SimpleJobLauncher;
import com.hogwai.batch.core.runtime.repository.InMemoryJobRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ListenerIntegrationTest {

    @Test
    void jobListenersShouldBeCalledInOrder() throws Exception {
        List<String> events = new ArrayList<>();

        JobExecutionListener listener = new JobExecutionListener() {
            @Override public void beforeJob(JobExecution exec) { events.add("beforeJob"); }
            @Override public void afterJob(JobExecution exec) { events.add("afterJob"); }
        };

        Iterator<String> data = List.of("a", "b").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .writer(items -> {})
                .build();

        Job job = new JobBuilder("testJob")
                .listener(listener)
                .start(step)
                .build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(events).containsExactly("beforeJob", "afterJob");
    }

    @Test
    void stepListenersShouldBeCalledInOrder() throws Exception {
        List<String> events = new ArrayList<>();

        StepExecutionListener listener = new StepExecutionListener() {
            @Override public void beforeStep(StepExecution exec) { events.add("beforeStep"); }
            @Override public void afterStep(StepExecution exec) { events.add("afterStep"); }
        };

        Iterator<String> data = List.of("a").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .writer(items -> {})
                .listener(listener)
                .build();

        Job job = new JobBuilder("testJob").start(step).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(events).containsExactly("beforeStep", "afterStep");
    }

    @Test
    void chunkListenersShouldBeCalledPerChunk() throws Exception {
        List<String> events = new ArrayList<>();

        ChunkListener listener = new ChunkListener() {
            @Override public void beforeChunk() { events.add("beforeChunk"); }
            @Override public void afterChunk() { events.add("afterChunk"); }
            @Override public void afterChunkError(Exception e) { events.add("afterChunkError"); }
        };

        Iterator<String> data = List.of("a", "b", "c").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(2)
                .reader(() -> data.hasNext() ? data.next() : null)
                .writer(items -> {})
                .chunkListener(listener)
                .build();

        Job job = new JobBuilder("testJob").start(step).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(events).containsExactly(
            "beforeChunk", "afterChunk",
            "beforeChunk", "afterChunk"
        );
    }
}
