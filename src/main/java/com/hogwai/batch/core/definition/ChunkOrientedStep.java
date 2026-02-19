package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.batch.core.ItemReader;
import com.hogwai.batch.core.ItemWriter;
import com.hogwai.batch.core.listener.ChunkListener;
import com.hogwai.batch.core.listener.ItemProcessListener;
import com.hogwai.batch.core.listener.ItemReadListener;
import com.hogwai.batch.core.listener.ItemWriteListener;
import com.hogwai.batch.core.listener.StepExecutionListener;
import com.hogwai.batch.core.runtime.StepContribution;
import com.hogwai.batch.core.runtime.StepExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Step} that reads, optionally processes, and writes items in fixed-size chunks.
 * Items are accumulated until the chunk size is reached, then written as a batch.
 *
 * @param <I> the input item type
 * @param <O> the output item type
 */
public class ChunkOrientedStep<I, O> implements Step {

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

    /**
     * Creates a chunk-oriented step without item-level listeners.
     *
     * @param name           the step name
     * @param chunkSize      the number of items per chunk
     * @param reader         the item reader
     * @param processor      the item processor, or {@code null} to pass items through
     * @param writer         the item writer
     * @param stepListeners  step-level lifecycle listeners
     * @param chunkListeners chunk-level lifecycle listeners
     */
    public ChunkOrientedStep(
            String name,
            int chunkSize,
            ItemReader<? extends I> reader,
            ItemProcessor<? super I, ? extends O> processor,
            ItemWriter<O> writer,
            List<StepExecutionListener> stepListeners,
            List<ChunkListener> chunkListeners
    ) {
        this(name, chunkSize, reader, processor, writer, stepListeners, chunkListeners,
                List.of(), List.of(), List.of());
    }

    /**
     * Creates a chunk-oriented step with full listener support.
     *
     * @param name                 the step name
     * @param chunkSize            the number of items per chunk
     * @param reader               the item reader
     * @param processor            the item processor, or {@code null} to pass items through
     * @param writer               the item writer
     * @param stepListeners        step-level lifecycle listeners
     * @param chunkListeners       chunk-level lifecycle listeners
     * @param itemReadListeners    read-level listeners
     * @param itemProcessListeners process-level listeners
     * @param itemWriteListeners   write-level listeners
     */
    public ChunkOrientedStep(
            String name,
            int chunkSize,
            ItemReader<? extends I> reader,
            ItemProcessor<? super I, ? extends O> processor,
            ItemWriter<O> writer,
            List<StepExecutionListener> stepListeners,
            List<ChunkListener> chunkListeners,
            List<ItemReadListener<I>> itemReadListeners,
            List<ItemProcessListener<I, O>> itemProcessListeners,
            List<ItemWriteListener<O>> itemWriteListeners
    ) {
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

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void execute(StepExecution stepExecution) {
        stepListeners.forEach(l -> l.beforeStep(stepExecution));

        StepContribution contribution = new StepContribution();
        List<O> chunk = new ArrayList<>(chunkSize);
        I item;

        while ((item = readItem()) != null) {
            contribution.incrementReadCount();
            O output;
            if (processor != null) {
                output = processItem(item);
                if (output == null) {
                    contribution.incrementFilterCount();
                    continue;
                }
            } else {
                output = (O) item;
            }
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

    private I readItem() {
        itemReadListeners.forEach(ItemReadListener::beforeRead);
        try {
            I item = reader.read();
            if (item != null) {
                itemReadListeners.forEach(l -> l.afterRead(item));
            }
            return item;
        } catch (Exception e) {
            itemReadListeners.forEach(l -> l.onReadError(e));
            throw e;
        }
    }

    private O processItem(I item) {
        itemProcessListeners.forEach(l -> l.beforeProcess(item));
        try {
            O result = processor.process(item);
            itemProcessListeners.forEach(l -> l.afterProcess(item, result));
            return result;
        } catch (Exception e) {
            itemProcessListeners.forEach(l -> l.onProcessError(item, e));
            throw e;
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
