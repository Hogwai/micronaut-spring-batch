package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobBuilderTest {

    @Test
    void shouldBuildJobWithNameAndSteps() {
        Step step1 = mock(Step.class);
        Step step2 = mock(Step.class);

        Job job = new JobBuilder("myJob")
                .start(step1)
                .next(step2)
                .build();

        assertThat(job.getName()).isEqualTo("myJob");
        assertThat(job.getSteps()).containsExactly(step1, step2);
    }

    @Test
    void shouldBuildJobWithNoSteps() {
        Job job = new JobBuilder("emptyJob").build();
        assertThat(job.getSteps()).isEmpty();
    }

    @Test
    void shouldReturnImmutableStepList() {
        Step step = mock(Step.class);
        Job job = new JobBuilder("job").start(step).build();

        assertThatThrownBy(() -> job.getSteps().add(mock(Step.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
