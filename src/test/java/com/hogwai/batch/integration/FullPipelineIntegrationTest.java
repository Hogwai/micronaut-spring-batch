package com.hogwai.batch.integration;

import com.hogwai.batch.core.RepeatStatus;
import com.hogwai.batch.core.builder.FlowBuilder;
import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Flow;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.listener.JobExecutionListener;
import com.hogwai.batch.core.listener.StepExecutionListener;
import com.hogwai.batch.core.policy.SimpleSkipPolicy;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.StepExecution;
import com.hogwai.batch.core.runtime.launcher.SimpleJobLauncher;
import com.hogwai.batch.core.runtime.repository.InMemoryJobRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FullPipelineIntegrationTest {

    @Test
    void shouldRunCompleteChunkPipelineWithListeners() throws Exception {
        List<String> events = new ArrayList<>();
        List<String> written = new ArrayList<>();

        Iterator<String> data = List.of("alice", "bob", "charlie").iterator();

        JobExecutionListener jobListener = new JobExecutionListener() {
            @Override public void beforeJob(JobExecution e) { events.add("JOB_START"); }
            @Override public void afterJob(JobExecution e) { events.add("JOB_END"); }
        };

        StepExecutionListener stepListener = new StepExecutionListener() {
            @Override public void beforeStep(StepExecution e) { events.add("STEP_START"); }
            @Override public void afterStep(StepExecution e) { events.add("STEP_END"); }
        };

        Step step = new StepBuilder("importStep")
                .<String, String>chunk(2)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(String::toUpperCase)
                .writer(written::addAll)
                .listener(stepListener)
                .build();

        Job job = new JobBuilder("importJob")
                .listener(jobListener)
                .start(step)
                .build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(written).containsExactly("ALICE", "BOB", "CHARLIE");
        assertThat(events).containsExactly("JOB_START", "STEP_START", "STEP_END", "JOB_END");

        StepExecution stepExec = exec.getStepExecutions().getFirst();
        assertThat(stepExec.getReadCount()).isEqualTo(3);
        assertThat(stepExec.getWriteCount()).isEqualTo(3);
        assertThat(stepExec.getCommitCount()).isEqualTo(2);
    }

    @Test
    void shouldRunMixedTaskletAndChunkJob() throws Exception {
        List<String> order = new ArrayList<>();
        List<String> written = new ArrayList<>();

        Step initStep = new StepBuilder("init").tasklet((c, ctx) -> {
            ctx.putString("initialized", "true");
            order.add("INIT");
            return RepeatStatus.FINISHED;
        }).build();

        Iterator<String> data = List.of("x", "y").iterator();
        Step processStep = new StepBuilder("process")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(String::toUpperCase)
                .writer(items -> { written.addAll(items); order.add("WRITE"); })
                .build();

        Step cleanupStep = new StepBuilder("cleanup").tasklet((c, ctx) -> {
            order.add("CLEANUP");
            return RepeatStatus.FINISHED;
        }).build();

        Job job = new JobBuilder("mixedJob")
                .start(initStep)
                .next(processStep)
                .next(cleanupStep)
                .build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(order).containsExactly("INIT", "WRITE", "CLEANUP");
        assertThat(written).containsExactly("X", "Y");
        assertThat(exec.getStepExecutions()).hasSize(3);
    }

    @Test
    void shouldRunJobWithSkipPolicy() throws Exception {
        Iterator<String> data = List.of("good", "bad", "good2").iterator();
        List<String> written = new ArrayList<>();

        SimpleSkipPolicy skipPolicy = new SimpleSkipPolicy(5);

        Step step = new StepBuilder("skipStep")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(item -> {
                    if ("bad".equals(item)) throw new IllegalArgumentException("bad");
                    return item.toUpperCase();
                })
                .writer(written::addAll)
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .build();

        Job job = new JobBuilder("skipJob").start(step).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(written).containsExactly("GOOD", "GOOD2");
    }

    @Test
    void shouldRunFlowWithConditionalTransitions() throws Exception {
        List<String> executed = new ArrayList<>();

        Step validateStep = new StepBuilder("validate").tasklet((c, ctx) -> {
            executed.add("VALIDATE");
            return RepeatStatus.FINISHED;
        }).build();

        Step processStep = new StepBuilder("process").tasklet((c, ctx) -> {
            executed.add("PROCESS");
            return RepeatStatus.FINISHED;
        }).build();

        Flow flow = new FlowBuilder("mainFlow")
                .start(validateStep)
                .on("COMPLETED").to(processStep)
                .end()
                .build();

        Job job = new JobBuilder("flowJob").start(flow).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(executed).containsExactly("VALIDATE", "PROCESS");
    }
}
