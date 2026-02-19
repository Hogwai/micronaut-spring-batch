package com.hogwai.batch.core.listener;

import java.util.List;

/**
 * Listener interface for receiving callbacks around each chunk write operation.
 *
 * @param <O> the type of items being written
 */
public interface ItemWriteListener<O> {

    /**
     * Called before a chunk of items is written.
     *
     * @param items the list of items about to be written
     */
    default void beforeWrite(List<O> items) {}

    /**
     * Called after a chunk of items has been successfully written.
     *
     * @param items the list of items that were written
     */
    default void afterWrite(List<O> items) {}

    /**
     * Called when an error occurs during the write operation.
     *
     * @param items     the list of items being written when the error occurred
     * @param exception the exception thrown during the write
     */
    default void onWriteError(List<O> items, Exception exception) {}
}
