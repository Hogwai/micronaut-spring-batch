package com.hogwai.batch.core.runtime.launcher;

import com.hogwai.batch.core.RepeatStatus;
import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.repository.InMemoryJobRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SimpleJobLauncherTest {

    @Test
    void shouldExecuteJobSuccessfully() throws Exception {
        List<String> written = new ArrayList<>();
        Iterator<String> data = List.of("hello").iterator();

        Step step = new StepBuilder("step1")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .writer(written::addAll)
                .build();

        Job job = new JobBuilder("job1").start(step).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(written).containsExactly("hello");
        assertThat(exec.getStartTime()).isNotNull();
        assertThat(exec.getEndTime()).isNotNull();
    }

    @Test
    void shouldMarkJobAsFailedOnException() {
        Step failStep = new StepBuilder("failStep")
                .tasklet((c, ctx) -> { throw new RuntimeException("boom"); })
                .build();

        Job job = new JobBuilder("failJob").start(failStep).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);

        assertThatThrownBy(() -> launcher.run(job, JobParameters.builder().toJobParameters()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldExecuteMultipleStepsInOrder() throws Exception {
        List<String> order = new ArrayList<>();

        Step step1 = new StepBuilder("step1").tasklet((c, ctx) -> {
            order.add("first"); return RepeatStatus.FINISHED;
        }).build();

        Step step2 = new StepBuilder("step2").tasklet((c, ctx) -> {
            order.add("second"); return RepeatStatus.FINISHED;
        }).build();

        Job job = new JobBuilder("multiStepJob").start(step1).next(step2).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(order).containsExactly("first", "second");
    }

    @Test
    void shouldStopOnFirstFailedStep() {
        List<String> order = new ArrayList<>();

        Step step1 = new StepBuilder("step1").tasklet((c, ctx) -> {
            order.add("first"); throw new RuntimeException("fail");
        }).build();

        Step step2 = new StepBuilder("step2").tasklet((c, ctx) -> {
            order.add("second"); return RepeatStatus.FINISHED;
        }).build();

        Job job = new JobBuilder("failJob").start(step1).next(step2).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);

        assertThatThrownBy(() -> launcher.run(job, JobParameters.builder().toJobParameters()))
                .isInstanceOf(RuntimeException.class);
        assertThat(order).containsExactly("first");
    }

    @Test
    void shouldCreateStepExecutionsForEachStep() throws Exception {
        Step step1 = new StepBuilder("step1").tasklet((c, ctx) -> RepeatStatus.FINISHED).build();
        Step step2 = new StepBuilder("step2").tasklet((c, ctx) -> RepeatStatus.FINISHED).build();

        Job job = new JobBuilder("job").start(step1).next(step2).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(exec.getStepExecutions()).hasSize(2);
        assertThat(exec.getStepExecutions().get(0).getStepName()).isEqualTo("step1");
        assertThat(exec.getStepExecutions().get(1).getStepName()).isEqualTo("step2");
        assertThat(exec.getStepExecutions()).allMatch(se -> se.getStatus() == BatchStatus.COMPLETED);
    }
}
