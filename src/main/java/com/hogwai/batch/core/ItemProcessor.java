package com.hogwai.batch.core;

/**
 * Transforms an input item into an output item. Returning {@code null} indicates
 * the item should be filtered and not passed to the writer.
 *
 * @param <I> the input item type
 * @param <O> the output item type
 * @see ItemReader
 * @see ItemWriter
 */
public interface ItemProcessor<I, O> {
    /**
     * Processes the given item, transforming it into the output type.
     *
     * @param item the input item to process
     * @return the processed item, or {@code null} to filter it out
     */
    O process(I item);
}