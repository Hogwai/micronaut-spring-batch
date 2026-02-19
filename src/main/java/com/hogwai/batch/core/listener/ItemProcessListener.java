package com.hogwai.batch.core.listener;

/**
 * Listener interface for receiving callbacks around each item processing operation.
 *
 * @param <I> the input item type
 * @param <O> the output item type after processing
 */
public interface ItemProcessListener<I, O> {

    /**
     * Called before an item is passed to the processor.
     *
     * @param item the item about to be processed
     */
    default void beforeProcess(I item) {}

    /**
     * Called after an item has been successfully processed.
     *
     * @param item   the input item
     * @param result the output produced by the processor
     */
    default void afterProcess(I item, O result) {}

    /**
     * Called when an error occurs during item processing.
     *
     * @param item      the item that caused the error
     * @param exception the exception thrown during processing
     */
    default void onProcessError(I item, Exception exception) {}
}
