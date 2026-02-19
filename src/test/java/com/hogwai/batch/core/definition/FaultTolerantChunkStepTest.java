package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.policy.SimpleRetryPolicy;
import com.hogwai.batch.core.policy.SimpleSkipPolicy;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.StepExecution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class FaultTolerantChunkStepTest {

    private StepExecution createStepExecution() {
        JobExecution jobExec = new JobExecution(1L, null, null);
        return new StepExecution(1L, "ftStep", jobExec);
    }

    @Test
    void shouldSkipFailedItemsInProcessor() throws Exception {
        Iterator<String> data = List.of("good", "bad", "good2").iterator();
        List<String> written = new ArrayList<>();

        SimpleSkipPolicy skipPolicy = new SimpleSkipPolicy(5);

        Step step = new StepBuilder("skipStep")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(item -> {
                    if ("bad".equals(item)) throw new IllegalArgumentException("bad item");
                    return item.toUpperCase();
                })
                .writer(written::addAll)
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .build();

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(written).containsExactly("GOOD", "GOOD2");
        assertThat(stepExec.getSkipCount()).isEqualTo(1);
    }

    @Test
    void shouldRetryFailedItemsInProcessor() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Iterator<String> data = List.of("flaky").iterator();
        List<String> written = new ArrayList<>();

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);

        Step step = new StepBuilder("retryStep")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(item -> {
                    if (attempts.incrementAndGet() < 3) throw new IllegalStateException("transient");
                    return item.toUpperCase();
                })
                .writer(written::addAll)
                .faultTolerant()
                .retryPolicy(retryPolicy)
                .build();

        StepExecution stepExec = createStepExecution();
        step.execute(stepExec);

        assertThat(written).containsExactly("FLAKY");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    void shouldFailWhenSkipLimitExceeded() {
        Iterator<String> data = List.of("bad1", "bad2", "bad3").iterator();

        SimpleSkipPolicy skipPolicy = new SimpleSkipPolicy(2);

        Step step = new StepBuilder("failStep")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(item -> { throw new IllegalArgumentException("always bad"); })
                .writer(items -> {})
                .faultTolerant()
                .skipPolicy(skipPolicy)
                .build();

        StepExecution stepExec = createStepExecution();
        assertThatThrownBy(() -> step.execute(stepExec))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
