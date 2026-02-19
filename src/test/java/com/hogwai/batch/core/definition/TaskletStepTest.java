package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.RepeatStatus;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.StepExecution;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class TaskletStepTest {

    private StepExecution createStepExecution() {
        JobExecution jobExec = new JobExecution(1L, null, null);
        return new StepExecution(1L, "taskletStep", jobExec);
    }

    @Test
    void shouldExecuteTaskletOnce() {
        AtomicInteger counter = new AtomicInteger(0);

        Step step = new StepBuilder("cleanupStep")
                .tasklet((contribution, context) -> {
                    counter.incrementAndGet();
                    return RepeatStatus.FINISHED;
                })
                .build();

        StepExecution stepExec = createStepExecution();
        assertThatNoException().isThrownBy(() -> step.execute(stepExec));
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void shouldRepeatUntilFinished() {
        AtomicInteger counter = new AtomicInteger(0);

        Step step = new StepBuilder("repeatStep")
                .tasklet((contribution, context) -> {
                    counter.incrementAndGet();
                    return counter.get() >= 3 ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
                })
                .build();

        StepExecution stepExec = createStepExecution();
        assertThatNoException().isThrownBy(() -> step.execute(stepExec));
        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void shouldPassExecutionContextToTasklet() {
        Step step = new StepBuilder("contextStep")
                .tasklet((contribution, context) -> {
                    context.putString("key", "value");
                    return RepeatStatus.FINISHED;
                })
                .build();

        StepExecution stepExec = createStepExecution();
        assertThatNoException().isThrownBy(() -> step.execute(stepExec));
        assertThat(stepExec.getExecutionContext().getString("key")).isEqualTo("value");
    }
}
