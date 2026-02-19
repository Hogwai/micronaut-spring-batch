package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.StepExecution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ChunkOrientedStepTest {

    private StepExecution createStepExecution() {
        JobExecution jobExec = new JobExecution(1L, null, null);
        return new StepExecution(1L, "testStep", jobExec);
    }

    @Test
    void shouldProcessAllItemsInSingleChunk() throws Exception {
        Iterator<String> data = List.of("a", "b").iterator();
        List<String> written = new ArrayList<>();

        ChunkOrientedStep<String, String> step = new ChunkOrientedStep<>(
                "step1", 10,
                () -> data.hasNext() ? data.next() : null,
                String::toUpperCase,
                written::addAll,
                List.of(), List.of()
        );

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(written).containsExactly("A", "B");
        assertThat(stepExec.getReadCount()).isEqualTo(2);
        assertThat(stepExec.getWriteCount()).isEqualTo(2);
        assertThat(stepExec.getCommitCount()).isEqualTo(1);
    }

    @Test
    void shouldSplitIntoMultipleChunks() throws Exception {
        Iterator<Integer> data = List.of(1, 2, 3, 4, 5).iterator();
        List<List<Integer>> chunks = new ArrayList<>();

        ChunkOrientedStep<Integer, Integer> step = new ChunkOrientedStep<>(
                "step1", 2,
                () -> data.hasNext() ? data.next() : null,
                i -> i * 10,
                items -> chunks.add(new ArrayList<>(items)),
                List.of(), List.of()
        );

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0)).containsExactly(10, 20);
        assertThat(chunks.get(1)).containsExactly(30, 40);
        assertThat(chunks.get(2)).containsExactly(50);
    }

    @Test
    void shouldWorkWithoutProcessor() throws Exception {
        Iterator<String> data = List.of("a", "b").iterator();
        List<String> written = new ArrayList<>();

        ChunkOrientedStep<String, String> step = new ChunkOrientedStep<>(
                "step1", 10,
                () -> data.hasNext() ? data.next() : null,
                null,
                written::addAll,
                List.of(), List.of()
        );

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(written).containsExactly("a", "b");
    }

    @Test
    void shouldHandleEmptyReader() throws Exception {
        List<String> written = new ArrayList<>();

        ChunkOrientedStep<String, String> step = new ChunkOrientedStep<>(
                "step1", 10,
                () -> null,
                String::toUpperCase,
                written::addAll,
                List.of(), List.of()
        );

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(written).isEmpty();
        assertThat(stepExec.getReadCount()).isZero();
    }

    @Test
    void shouldFilterNullProcessorResults() throws Exception {
        Iterator<String> data = List.of("keep", "drop", "keep2").iterator();
        List<String> written = new ArrayList<>();

        ChunkOrientedStep<String, String> step = new ChunkOrientedStep<>(
                "step1", 10,
                () -> data.hasNext() ? data.next() : null,
                item -> "drop".equals(item) ? null : item,
                written::addAll,
                List.of(), List.of()
        );

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(written).containsExactly("keep", "keep2");
        assertThat(stepExec.getFilterCount()).isEqualTo(1);
    }
}
