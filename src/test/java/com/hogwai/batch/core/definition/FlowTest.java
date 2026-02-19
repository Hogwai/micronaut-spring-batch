package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.RepeatStatus;
import com.hogwai.batch.core.builder.FlowBuilder;
import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.launcher.SimpleJobLauncher;
import com.hogwai.batch.core.runtime.repository.InMemoryJobRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class FlowTest {

    @Test
    void shouldFollowCompletedTransition() throws Exception {
        List<String> executed = new ArrayList<>();

        Step stepA = new StepBuilder("stepA").tasklet((c, ctx) -> {
            executed.add("A"); return RepeatStatus.FINISHED;
        }).build();

        Step stepB = new StepBuilder("stepB").tasklet((c, ctx) -> {
            executed.add("B"); return RepeatStatus.FINISHED;
        }).build();

        Step stepC = new StepBuilder("stepC").tasklet((c, ctx) -> {
            executed.add("C"); return RepeatStatus.FINISHED;
        }).build();

        Flow flow = new FlowBuilder("testFlow")
                .start(stepA)
                .on("COMPLETED").to(stepB)
                .on("FAILED").to(stepC)
                .end()
                .build();

        Job job = new JobBuilder("flowJob").start(flow).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(executed).containsExactly("A", "B");
    }

    @Test
    void shouldFollowFailedTransition() throws Exception {
        List<String> executed = new ArrayList<>();

        Step failingStep = new StepBuilder("failStep").tasklet((c, ctx) -> {
            executed.add("FAIL");
            throw new RuntimeException("intentional failure");
        }).build();

        Step recoveryStep = new StepBuilder("recoveryStep").tasklet((c, ctx) -> {
            executed.add("RECOVERY"); return RepeatStatus.FINISHED;
        }).build();

        Step neverStep = new StepBuilder("neverStep").tasklet((c, ctx) -> {
            executed.add("NEVER"); return RepeatStatus.FINISHED;
        }).build();

        Flow flow = new FlowBuilder("failFlow")
                .start(failingStep)
                .on("COMPLETED").to(neverStep)
                .on("FAILED").to(recoveryStep)
                .end()
                .build();

        Job job = new JobBuilder("failFlowJob").start(flow).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        JobExecution exec = launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(executed).containsExactly("FAIL", "RECOVERY");
    }

    @Test
    void shouldSupportWildcardTransition() throws Exception {
        List<String> executed = new ArrayList<>();

        Step stepA = new StepBuilder("stepA").tasklet((c, ctx) -> {
            executed.add("A"); return RepeatStatus.FINISHED;
        }).build();

        Step fallback = new StepBuilder("fallback").tasklet((c, ctx) -> {
            executed.add("FALLBACK"); return RepeatStatus.FINISHED;
        }).build();

        Flow flow = new FlowBuilder("wildcardFlow")
                .start(stepA)
                .on("*").to(fallback)
                .end()
                .build();

        Job job = new JobBuilder("wildcardJob").start(flow).build();

        InMemoryJobRepository repo = new InMemoryJobRepository();
        SimpleJobLauncher launcher = new SimpleJobLauncher(repo);
        launcher.run(job, JobParameters.builder().toJobParameters());

        assertThat(executed).containsExactly("A", "FALLBACK");
    }
}
