package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.definition.Step;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StepBuilderTest {

    @Test
    void shouldFailWithoutReader() {
        assertThatThrownBy(() ->
            new StepBuilder("step")
                .<String, String>chunk(10)
                .writer(items -> {})
                .build()
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("reader is required");
    }

    @Test
    void shouldFailWithoutWriter() {
        assertThatThrownBy(() ->
            new StepBuilder("step")
                .<String, String>chunk(10)
                .reader(() -> null)
                .build()
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("writer is required");
    }

    @Test
    void shouldBuildValidChunkStep() {
        Step step = new StepBuilder("step")
                .<String, String>chunk(5)
                .reader(() -> null)
                .writer(items -> {})
                .build();

        assertThat(step.getName()).isEqualTo("step");
    }
}
