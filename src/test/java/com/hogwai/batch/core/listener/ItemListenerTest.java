package com.hogwai.batch.core.listener;

import com.hogwai.batch.core.builder.JobBuilder;
import com.hogwai.batch.core.builder.StepBuilder;
import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.definition.Job;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.runtime.launcher.SimpleJobLauncher;
import com.hogwai.batch.core.runtime.repository.InMemoryJobRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemListenerTest {

    @Test
    void itemReadListenerShouldBeCalledForEachItemRead() throws Exception {
        List<String> events = new ArrayList<>();

        ItemReadListener<String> readListener = new ItemReadListener<>() {
            @Override
            public void beforeRead() {
                events.add("beforeRead");
            }

            @Override
            public void afterRead(String item) {
                events.add("afterRead:" + item);
            }
        };

        Iterator<String> data = List.of("a", "b", "c").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .writer(items -> {})
                .itemReadListener(readListener)
                .build();

        Job job = new JobBuilder("testJob").start(step).build();
        new SimpleJobLauncher(new InMemoryJobRepository())
                .run(job, JobParameters.builder().toJobParameters());

        // beforeRead is called 4 times: once for each of the 3 items + once for the null-returning read
        assertThat(events).containsExactly(
                "beforeRead", "afterRead:a",
                "beforeRead", "afterRead:b",
                "beforeRead", "afterRead:c",
                "beforeRead"
        );
    }

    @Test
    void itemProcessListenerShouldBeCalledForEachItemProcessed() throws Exception {
        List<String> events = new ArrayList<>();

        ItemProcessListener<String, String> processListener = new ItemProcessListener<>() {
            @Override
            public void beforeProcess(String item) {
                events.add("beforeProcess:" + item);
            }

            @Override
            public void afterProcess(String item, String result) {
                events.add("afterProcess:" + item + "->" + result);
            }
        };

        Iterator<String> data = List.of("a", "b").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(String::toUpperCase)
                .writer(items -> {})
                .itemProcessListener(processListener)
                .build();

        Job job = new JobBuilder("testJob").start(step).build();
        new SimpleJobLauncher(new InMemoryJobRepository())
                .run(job, JobParameters.builder().toJobParameters());

        assertThat(events).containsExactly(
                "beforeProcess:a", "afterProcess:a->A",
                "beforeProcess:b", "afterProcess:b->B"
        );
    }

    @Test
    void itemWriteListenerShouldBeCalledForEachChunkWritten() throws Exception {
        List<String> events = new ArrayList<>();

        ItemWriteListener<String> writeListener = new ItemWriteListener<>() {
            @Override
            public void beforeWrite(List<String> items) {
                events.add("beforeWrite:" + items);
            }

            @Override
            public void afterWrite(List<String> items) {
                events.add("afterWrite:" + items);
            }
        };

        Iterator<String> data = List.of("a", "b", "c").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(2)
                .reader(() -> data.hasNext() ? data.next() : null)
                .writer(items -> {})
                .itemWriteListener(writeListener)
                .build();

        Job job = new JobBuilder("testJob").start(step).build();
        new SimpleJobLauncher(new InMemoryJobRepository())
                .run(job, JobParameters.builder().toJobParameters());

        assertThat(events).containsExactly(
                "beforeWrite:[a, b]", "afterWrite:[a, b]",
                "beforeWrite:[c]", "afterWrite:[c]"
        );
    }

    @Test
    void skipListenerShouldBeCalledWhenItemIsSkippedDuringProcessing() throws Exception {
        List<String> events = new ArrayList<>();

        SkipListener<String, String> skipListener = new SkipListener<>() {
            @Override
            public void onSkipInProcess(String item, Throwable t) {
                events.add("onSkipInProcess:" + item + ":" + t.getMessage());
            }
        };

        ItemProcessListener<String, String> processListener = new ItemProcessListener<>() {
            @Override
            public void beforeProcess(String item) {
                events.add("beforeProcess:" + item);
            }

            @Override
            public void afterProcess(String item, String result) {
                events.add("afterProcess:" + item + "->" + result);
            }

            @Override
            public void onProcessError(String item, Exception e) {
                events.add("onProcessError:" + item);
            }
        };

        Iterator<String> data = List.of("a", "BAD", "c").iterator();
        Step step = new StepBuilder("step1")
                .<String, String>chunk(10)
                .reader(() -> data.hasNext() ? data.next() : null)
                .processor(item -> {
                    if ("BAD".equals(item)) throw new RuntimeException("bad item");
                    return item.toUpperCase();
                })
                .writer(items -> {})
                .itemProcessListener(processListener)
                .faultTolerant()
                .skipPolicy((t, skipCount) -> skipCount < 3)
                .skipListener(skipListener)
                .build();

        Job job = new JobBuilder("testJob").start(step).build();
        new SimpleJobLauncher(new InMemoryJobRepository())
                .run(job, JobParameters.builder().toJobParameters());

        assertThat(events).containsExactly(
                "beforeProcess:a", "afterProcess:a->A",
                "beforeProcess:BAD", "onProcessError:BAD", "onSkipInProcess:BAD:bad item",
                "beforeProcess:c", "afterProcess:c->C"
        );
    }
}
