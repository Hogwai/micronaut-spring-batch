package com.hogwai.batch.core.listener;

/**
 * Listener interface for receiving callbacks around each item read operation.
 *
 * @param <T> the type of item being read
 */
public interface ItemReadListener<T> {

    /**
     * Called before an item is read from the reader.
     */
    default void beforeRead() {}

    /**
     * Called after an item has been successfully read.
     *
     * @param item the item that was read
     */
    default void afterRead(T item) {}

    /**
     * Called when an error occurs during a read operation.
     *
     * @param exception the exception thrown during the read
     */
    default void onReadError(Exception exception) {}
}
