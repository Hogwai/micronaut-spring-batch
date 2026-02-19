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

/**
 * Fluent builder for constructing {@link Step} instances. Supports chunk-oriented,
 * tasklet-based, and fault-tolerant step configurations.
 *
 * @see ChunkStepBuilder
 * @see TaskletStepBuilder
 * @see FaultTolerantChunkStepBuilder
 */
public class StepBuilder {

    private final String name;

    /**
     * Creates a new step builder with the given step name.
     *
     * @param name the unique name for the step
     */
    public StepBuilder(String name) {
        this.name = name;
    }

    /**
     * Begins configuring a chunk-oriented step with the specified chunk size.
     *
     * @param chunkSize the number of items to process per chunk
     * @param <I>       the input item type
     * @param <O>       the output item type
     * @return a {@link ChunkStepBuilder} for further configuration
     */
    public <I, O> ChunkStepBuilder<I, O> chunk(int chunkSize) {
        return new ChunkStepBuilder<>(name, chunkSize);
    }

    /**
     * Begins configuring a tasklet-based step.
     *
     * @param tasklet the tasklet to execute
     * @return a {@link TaskletStepBuilder} for further configuration
     */
    public TaskletStepBuilder tasklet(Tasklet tasklet) {
        return new TaskletStepBuilder(name, tasklet);
    }

    /**
     * Builder for chunk-oriented steps that read, optionally process, and write items in chunks.
     *
     * @param <I> the input item type
     * @param <O> the output item type
     */
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

        /**
         * Sets the item reader for this chunk step.
         *
         * @param reader the reader that supplies input items
         * @return this builder
         */
        public ChunkStepBuilder<I, O> reader(ItemReader<? extends I> reader) {
            this.reader = reader;
            return this;
        }

        /**
         * Sets the optional item processor for this chunk step.
         *
         * @param processor the processor that transforms input items to output items
         * @return this builder
         */
        public ChunkStepBuilder<I, O> processor(ItemProcessor<? super I, ? extends O> processor) {
            this.processor = processor;
            return this;
        }

        /**
         * Sets the item writer for this chunk step.
         *
         * @param writer the writer that receives processed items
         * @return this builder
         */
        public ChunkStepBuilder<I, O> writer(ItemWriter<O> writer) {
            this.writer = writer;
            return this;
        }

        /**
         * Registers a step execution listener.
         *
         * @param listener the step execution listener
         * @return this builder
         */
        public ChunkStepBuilder<I, O> listener(StepExecutionListener listener) {
            this.stepListeners.add(listener);
            return this;
        }

        /**
         * Registers a chunk listener.
         *
         * @param listener the chunk listener
         * @return this builder
         */
        public ChunkStepBuilder<I, O> chunkListener(ChunkListener listener) {
            this.chunkListeners.add(listener);
            return this;
        }

        /**
         * Registers an item read listener.
         *
         * @param listener the item read listener
         * @return this builder
         */
        public ChunkStepBuilder<I, O> itemReadListener(ItemReadListener<I> listener) {
            this.itemReadListeners.add(listener);
            return this;
        }

        /**
         * Registers an item process listener.
         *
         * @param listener the item process listener
         * @return this builder
         */
        public ChunkStepBuilder<I, O> itemProcessListener(ItemProcessListener<I, O> listener) {
            this.itemProcessListeners.add(listener);
            return this;
        }

        /**
         * Registers an item write listener.
         *
         * @param listener the item write listener
         * @return this builder
         */
        public ChunkStepBuilder<I, O> itemWriteListener(ItemWriteListener<O> listener) {
            this.itemWriteListeners.add(listener);
            return this;
        }

        /**
         * Upgrades this builder to a fault-tolerant configuration supporting skip and retry policies.
         *
         * @return a {@link FaultTolerantChunkStepBuilder} pre-populated with the current configuration
         */
        public FaultTolerantChunkStepBuilder<I, O> faultTolerant() {
            return new FaultTolerantChunkStepBuilder<>(name, chunkSize, reader, processor, writer,
                    stepListeners, chunkListeners, itemReadListeners, itemProcessListeners, itemWriteListeners);
        }

        /**
         * Builds the chunk-oriented step.
         *
         * @return the constructed {@link Step}
         * @throws IllegalStateException if reader or writer is not set
         */
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

    /**
     * Builder for tasklet-based steps that execute a single {@link Tasklet}.
     */
    public static class TaskletStepBuilder {
        private final String name;
        private final Tasklet tasklet;
        private final List<StepExecutionListener> listeners = new ArrayList<>();

        private TaskletStepBuilder(String name, Tasklet tasklet) {
            this.name = name;
            this.tasklet = tasklet;
        }

        /**
         * Registers a step execution listener.
         *
         * @param listener the step execution listener
         * @return this builder
         */
        public TaskletStepBuilder listener(StepExecutionListener listener) {
            listeners.add(listener);
            return this;
        }

        /**
         * Builds the tasklet step.
         *
         * @return the constructed {@link Step}
         */
        public Step build() {
            return new TaskletStep(name, tasklet, List.copyOf(listeners));
        }
    }

    /**
     * Builder for fault-tolerant chunk steps that support skip, retry, and backoff policies.
     *
     * @param <I> the input item type
     * @param <O> the output item type
     */
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

        /**
         * Sets the skip policy that determines which exceptions are skippable and the skip limit.
         *
         * @param skipPolicy the skip policy
         * @return this builder
         */
        public FaultTolerantChunkStepBuilder<I, O> skipPolicy(SkipPolicy skipPolicy) {
            this.skipPolicy = skipPolicy;
            return this;
        }

        /**
         * Sets the retry policy that determines which exceptions are retryable and the retry limit.
         *
         * @param retryPolicy the retry policy
         * @return this builder
         */
        public FaultTolerantChunkStepBuilder<I, O> retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        /**
         * Sets the backoff policy that controls the delay between retry attempts.
         *
         * @param backoffPolicy the backoff policy
         * @return this builder
         */
        public FaultTolerantChunkStepBuilder<I, O> backoffPolicy(BackoffPolicy backoffPolicy) {
            this.backoffPolicy = backoffPolicy;
            return this;
        }

        /**
         * Registers a skip listener to be notified when items are skipped.
         *
         * @param listener the skip listener
         * @return this builder
         */
        public FaultTolerantChunkStepBuilder<I, O> skipListener(SkipListener<I, O> listener) {
            this.skipListeners.add(listener);
            return this;
        }

        /**
         * Builds the fault-tolerant chunk step.
         *
         * @return the constructed {@link Step}
         */
        public Step build() {
            return new FaultTolerantChunkStep<>(name, chunkSize, reader, processor, writer,
                    skipPolicy, retryPolicy, backoffPolicy,
                    List.copyOf(stepListeners), List.copyOf(chunkListeners),
                    List.copyOf(itemReadListeners), List.copyOf(itemProcessListeners),
                    List.copyOf(itemWriteListeners), List.copyOf(skipListeners));
        }
    }
}
