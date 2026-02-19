package com.hogwai.batch.core.runtime;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StepExecutionTest {

    private StepExecution createStepExecution() {
        JobExecution jobExec = new JobExecution(1L, null, null);
        return new StepExecution(1L, "testStep", jobExec);
    }

    @Test
    void shouldApplyContribution() {
        StepExecution stepExec = createStepExecution();
        StepContribution contribution = new StepContribution();
        contribution.incrementReadCount();
        contribution.incrementReadCount();
        contribution.incrementWriteCount(2);

        stepExec.apply(contribution);

        assertThat(stepExec.getReadCount()).isEqualTo(2);
        assertThat(stepExec.getWriteCount()).isEqualTo(2);
    }

    @Test
    void shouldAccumulateMultipleContributions() {
        StepExecution stepExec = createStepExecution();

        StepContribution c1 = new StepContribution();
        c1.incrementReadCount();
        c1.incrementWriteCount(1);
        stepExec.apply(c1);

        StepContribution c2 = new StepContribution();
        c2.incrementReadCount();
        c2.incrementReadCount();
        c2.incrementWriteCount(2);
        stepExec.apply(c2);

        assertThat(stepExec.getReadCount()).isEqualTo(3);
        assertThat(stepExec.getWriteCount()).isEqualTo(3);
    }

    @Test
    void shouldTrackSkipCounts() {
        StepExecution stepExec = createStepExecution();
        StepContribution contribution = new StepContribution();
        contribution.incrementSkipCountInRead();
        contribution.incrementSkipCountInProcess();
        contribution.incrementSkipCountInWrite();

        stepExec.apply(contribution);

        assertThat(stepExec.getSkipCount()).isEqualTo(3);
    }

    @Test
    void shouldStartWithStartingStatus() {
        StepExecution stepExec = createStepExecution();
        assertThat(stepExec.getStatus()).isEqualTo(BatchStatus.STARTING);
    }
}
