package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.batch.core.ItemReader;
import com.hogwai.batch.core.ItemWriter;
import com.hogwai.batch.core.Tasklet;
import com.hogwai.batch.core.definition.ChunkOrientedStep;
import com.hogwai.batch.core.definition.FaultTolerantChunkStep;
import com.hogwai.batch.core.definition.Step;
import com.hogwai.batch.core.definition.TaskletStep;
import com.hogwai.batch.core.listener.ChunkListener;
import com.hogwai.batch.core.listener.ItemProcessListener;
import com.hogwai.batch.core.listener.ItemReadListener;
import com.hogwai.batch.core.listener.ItemWriteListener;
import com.hogwai.batch.core.listener.SkipListener;
import com.hogwai.batch.core.listener.StepExecutionListener;
import com.hogwai.batch.core.policy.BackoffPolicy;
import com.hogwai.batch.core.policy.RetryPolicy;
import com.hogwai.batch.core.policy.SkipPolicy;

import java.util.ArrayList;
import java.util.List;

public class StepBuilder {

    private final String name;

    public StepBuilder(String name) {
        this.name = name;
    }

    public <I, O> ChunkStepBuilder<I, O> chunk(int chunkSize) {
        return new ChunkStepBuilder<>(name, chunkSize);
    }

    public TaskletStepBuilder tasklet(Tasklet tasklet) {
        return new TaskletStepBuilder(name, tasklet);
    }

    public static class ChunkStepBuilder<I, O> {
        private final String name;
        private final int chunkSize;
        private ItemReader<? extends I> reader;
        private ItemProcessor<? super I, ? extends O> processor;
        private ItemWriter<O> writer;
        private final List<StepExecutionListener> stepListeners = new ArrayList<>();
        private final List<ChunkListener> chunkListeners = new ArrayList<>();
        private final List<ItemReadListener<I>> itemReadListeners = new ArrayList<>();
        private final List<ItemProcessListener<I, O>> itemProcessListeners = new ArrayList<>();
        private final List<ItemWriteListener<O>> itemWriteListeners = new ArrayList<>();

        private ChunkStepBuilder(String name, int chunkSize) {
            this.name = name;
            this.chunkSize = chunkSize;
        }

        public ChunkStepBuilder<I, O> reader(ItemReader<? extends I> reader) {
            this.reader = reader;
            return this;
        }

        public ChunkStepBuilder<I, O> processor(ItemProcessor<? super I, ? extends O> processor) {
            this.processor = processor;
            return this;
        }

        public ChunkStepBuilder<I, O> writer(ItemWriter<O> writer) {
            this.writer = writer;
            return this;
        }

        public ChunkStepBuilder<I, O> listener(StepExecutionListener listener) {
            this.stepListeners.add(listener);
            return this;
        }

        public ChunkStepBuilder<I, O> chunkListener(ChunkListener listener) {
            this.chunkListeners.add(listener);
            return this;
        }

        public ChunkStepBuilder<I, O> itemReadListener(ItemReadListener<I> listener) {
            this.itemReadListeners.add(listener);
            return this;
        }

        public ChunkStepBuilder<I, O> itemProcessListener(ItemProcessListener<I, O> listener) {
            this.itemProcessListeners.add(listener);
            return this;
        }

        public ChunkStepBuilder<I, O> itemWriteListener(ItemWriteListener<O> listener) {
            this.itemWriteListeners.add(listener);
            return this;
        }

        public FaultTolerantChunkStepBuilder<I, O> faultTolerant() {
            return new FaultTolerantChunkStepBuilder<>(name, chunkSize, reader, processor, writer,
                    stepListeners, chunkListeners, itemReadListeners, itemProcessListeners, itemWriteListeners);
        }

        public Step build() {
            if (reader == null) throw new IllegalStateException("reader is required");
            if (writer == null) throw new IllegalStateException("writer is required");

            return new ChunkOrientedStep<>(
                    name, chunkSize, reader, processor, writer,
                    List.copyOf(stepListeners), List.copyOf(chunkListeners),
                    List.copyOf(itemReadListeners), List.copyOf(itemProcessListeners),
                    List.copyOf(itemWriteListeners)
            );
        }
    }

    public static class TaskletStepBuilder {
        private final String name;
        private final Tasklet tasklet;
        private final List<StepExecutionListener> listeners = new ArrayList<>();

        private TaskletStepBuilder(String name, Tasklet tasklet) {
            this.name = name;
            this.tasklet = tasklet;
        }

        public TaskletStepBuilder listener(StepExecutionListener listener) {
            listeners.add(listener);
            return this;
        }

        public Step build() {
            return new TaskletStep(name, tasklet, List.copyOf(listeners));
        }
    }

    public static class FaultTolerantChunkStepBuilder<I, O> {
        private final String name;
        private final int chunkSize;
        private final ItemReader<? extends I> reader;
        private final ItemProcessor<? super I, ? extends O> processor;
        private final ItemWriter<O> writer;
        private final List<StepExecutionListener> stepListeners;
        private final List<ChunkListener> chunkListeners;
        private final List<ItemReadListener<I>> itemReadListeners;
        private final List<ItemProcessListener<I, O>> itemProcessListeners;
        private final List<ItemWriteListener<O>> itemWriteListeners;
        private final List<SkipListener<I, O>> skipListeners = new ArrayList<>();
        private SkipPolicy skipPolicy;
        private RetryPolicy retryPolicy;
        private BackoffPolicy backoffPolicy;

        FaultTolerantChunkStepBuilder(String name, int chunkSize,
                ItemReader<? extends I> reader, ItemProcessor<? super I, ? extends O> processor,
                ItemWriter<O> writer, List<StepExecutionListener> stepListeners,
                List<ChunkListener> chunkListeners,
                List<ItemReadListener<I>> itemReadListeners,
                List<ItemProcessListener<I, O>> itemProcessListeners,
                List<ItemWriteListener<O>> itemWriteListeners) {
            this.name = name;
            this.chunkSize = chunkSize;
            this.reader = reader;
            this.processor = processor;
            this.writer = writer;
            this.stepListeners = stepListeners;
            this.chunkListeners = chunkListeners;
            this.itemReadListeners = itemReadListeners;
            this.itemProcessListeners = itemProcessListeners;
            this.itemWriteListeners = itemWriteListeners;
        }

        public FaultTolerantChunkStepBuilder<I, O> skipPolicy(SkipPolicy skipPolicy) {
            this.skipPolicy = skipPolicy;
            return this;
        }

        public FaultTolerantChunkStepBuilder<I, O> retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public FaultTolerantChunkStepBuilder<I, O> backoffPolicy(BackoffPolicy backoffPolicy) {
            this.backoffPolicy = backoffPolicy;
            return this;
        }

        public FaultTolerantChunkStepBuilder<I, O> skipListener(SkipListener<I, O> listener) {
            this.skipListeners.add(listener);
            return this;
        }

        public Step build() {
            return new FaultTolerantChunkStep<>(name, chunkSize, reader, processor, writer,
                    skipPolicy, retryPolicy, backoffPolicy,
                    List.copyOf(stepListeners), List.copyOf(chunkListeners),
                    List.copyOf(itemReadListeners), List.copyOf(itemProcessListeners),
                    List.copyOf(itemWriteListeners), List.copyOf(skipListeners));
        }
    }
}
