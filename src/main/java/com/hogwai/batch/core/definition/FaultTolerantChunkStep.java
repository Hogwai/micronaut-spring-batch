package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.batch.core.ItemReader;
import com.hogwai.batch.core.ItemWriter;
import com.hogwai.batch.core.listener.ChunkListener;
import com.hogwai.batch.core.listener.ItemProcessListener;
import com.hogwai.batch.core.listener.ItemReadListener;
import com.hogwai.batch.core.listener.ItemWriteListener;
import com.hogwai.batch.core.listener.SkipListener;
import com.hogwai.batch.core.listener.StepExecutionListener;
import com.hogwai.batch.core.policy.BackoffPolicy;
import com.hogwai.batch.core.policy.RetryPolicy;
import com.hogwai.batch.core.policy.SkipPolicy;
import com.hogwai.batch.core.runtime.StepContribution;
import com.hogwai.batch.core.runtime.StepExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * A chunk-oriented {@link Step} with fault-tolerance support, including configurable
 * skip and retry policies for handling transient or expected errors during processing.
 *
 * @param <I> the input item type
 * @param <O> the output item type
 * @see SkipPolicy
 * @see RetryPolicy
 */
public class FaultTolerantChunkStep<I, O> implements Step {

    private final String name;
    private final int chunkSize;
    private final ItemReader<? extends I> reader;
    private final ItemProcessor<? super I, ? extends O> processor;
    private final ItemWriter<O> writer;
    private final SkipPolicy skipPolicy;
    private final RetryPolicy retryPolicy;
    private final BackoffPolicy backoffPolicy;
    private final List<StepExecutionListener> stepListeners;
    private final List<ChunkListener> chunkListeners;
    private final List<ItemReadListener<I>> itemReadListeners;
    private final List<ItemProcessListener<I, O>> itemProcessListeners;
    private final List<ItemWriteListener<O>> itemWriteListeners;
    private final List<SkipListener<I, O>> skipListeners;

    /**
     * Creates a fault-tolerant chunk step without item-level or skip listeners.
     *
     * @param name           the step name
     * @param chunkSize      the number of items per chunk
     * @param reader         the item reader
     * @param processor      the item processor, or {@code null} to pass items through
     * @param writer         the item writer
     * @param skipPolicy     policy determining whether to skip on error
     * @param retryPolicy    policy determining whether to retry on error
     * @param backoffPolicy  policy for delay between retries
     * @param stepListeners  step-level lifecycle listeners
     * @param chunkListeners chunk-level lifecycle listeners
     */
    public FaultTolerantChunkStep(
            String name, int chunkSize,
            ItemReader<? extends I> reader,
            ItemProcessor<? super I, ? extends O> processor,
            ItemWriter<O> writer,
            SkipPolicy skipPolicy,
            RetryPolicy retryPolicy,
            BackoffPolicy backoffPolicy,
            List<StepExecutionListener> stepListeners,
            List<ChunkListener> chunkListeners
    ) {
        this(name, chunkSize, reader, processor, writer, skipPolicy, retryPolicy, backoffPolicy,
                stepListeners, chunkListeners, List.of(), List.of(), List.of(), List.of());
    }

    /**
     * Creates a fault-tolerant chunk step with full listener support.
     *
     * @param name                 the step name
     * @param chunkSize            the number of items per chunk
     * @param reader               the item reader
     * @param processor            the item processor, or {@code null} to pass items through
     * @param writer               the item writer
     * @param skipPolicy           policy determining whether to skip on error
     * @param retryPolicy          policy determining whether to retry on error
     * @param backoffPolicy        policy for delay between retries
     * @param stepListeners        step-level lifecycle listeners
     * @param chunkListeners       chunk-level lifecycle listeners
     * @param itemReadListeners    read-level listeners
     * @param itemProcessListeners process-level listeners
     * @param itemWriteListeners   write-level listeners
     * @param skipListeners        skip event listeners
     */
    public FaultTolerantChunkStep(
            String name, int chunkSize,
            ItemReader<? extends I> reader,
            ItemProcessor<? super I, ? extends O> processor,
            ItemWriter<O> writer,
            SkipPolicy skipPolicy,
            RetryPolicy retryPolicy,
            BackoffPolicy backoffPolicy,
            List<StepExecutionListener> stepListeners,
            List<ChunkListener> chunkListeners,
            List<ItemReadListener<I>> itemReadListeners,
            List<ItemProcessListener<I, O>> itemProcessListeners,
            List<ItemWriteListener<O>> itemWriteListeners,
            List<SkipListener<I, O>> skipListeners
    ) {
        this.name = name;
        this.chunkSize = chunkSize;
        this.reader = reader;
        this.processor = processor;
        this.writer = writer;
        this.skipPolicy = skipPolicy;
        this.retryPolicy = retryPolicy;
        this.backoffPolicy = backoffPolicy;
        this.stepListeners = stepListeners;
        this.chunkListeners = chunkListeners;
        this.itemReadListeners = itemReadListeners;
        this.itemProcessListeners = itemProcessListeners;
        this.itemWriteListeners = itemWriteListeners;
        this.skipListeners = skipListeners;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() { return name; }

    /** {@inheritDoc} */
    @Override
    public void execute(StepExecution stepExecution) throws Exception {
        stepListeners.forEach(l -> l.beforeStep(stepExecution));
        StepContribution contribution = new StepContribution();
        List<O> chunk = new ArrayList<>(chunkSize);
        I item;

        while ((item = readItem(contribution)) != null) {
            O output = processItem(item, contribution);
            if (output == null) continue;
            chunk.add(output);

            if (chunk.size() >= chunkSize) {
                writeChunk(chunk, contribution, stepExecution);
                chunk.clear();
            }
        }

        if (!chunk.isEmpty()) {
            writeChunk(chunk, contribution, stepExecution);
        }

        stepExecution.apply(contribution);
        stepListeners.forEach(l -> l.afterStep(stepExecution));
    }

    private I readItem(StepContribution contribution) {
        itemReadListeners.forEach(ItemReadListener::beforeRead);
        try {
            I item = reader.read();
            if (item != null) {
                contribution.incrementReadCount();
                itemReadListeners.forEach(l -> l.afterRead(item));
            }
            return item;
        } catch (Exception e) {
            itemReadListeners.forEach(l -> l.onReadError(e));
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private O processItem(I item, StepContribution contribution) throws Exception {
        if (processor == null) return (O) item;

        int attempts = 0;
        while (true) {
            try {
                attempts++;
                itemProcessListeners.forEach(l -> l.beforeProcess(item));
                O result = processor.process(item);
                if (result == null) contribution.incrementFilterCount();
                itemProcessListeners.forEach(l -> l.afterProcess(item, result));
                return result;
            } catch (Exception e) {
                itemProcessListeners.forEach(l -> l.onProcessError(item, e));
                if (retryPolicy != null && retryPolicy.shouldRetry(e, attempts)) {
                    if (backoffPolicy != null) backoffPolicy.backoff(attempts);
                    continue;
                }
                if (skipPolicy != null && skipPolicy.shouldSkip(e, contribution.getSkipCount())) {
                    contribution.incrementSkipCountInProcess();
                    skipListeners.forEach(l -> l.onSkipInProcess(item, e));
                    return null;
                }
                throw e;
            }
        }
    }

    private void writeChunk(List<O> chunk, StepContribution contribution, StepExecution stepExecution) {
        chunkListeners.forEach(ChunkListener::beforeChunk);
        itemWriteListeners.forEach(l -> l.beforeWrite(chunk));
        try {
            writer.write(chunk);
            itemWriteListeners.forEach(l -> l.afterWrite(chunk));
            contribution.incrementWriteCount(chunk.size());
            stepExecution.incrementCommitCount();
            chunkListeners.forEach(ChunkListener::afterChunk);
        } catch (Exception e) {
            itemWriteListeners.forEach(l -> l.onWriteError(chunk, e));
            chunkListeners.forEach(l -> l.afterChunkError(e));
            throw e;
        }
    }
}
