package com.hogwai.batch.core.listener;

/**
 * Listener interface for receiving callbacks when items are skipped during read, process, or write phases.
 *
 * @param <T> the input item type (used during read and process skips)
 * @param <S> the output item type (used during write skips)
 */
public interface SkipListener<T, S> {

    /**
     * Called when an item is skipped during the read phase.
     *
     * @param t the throwable that caused the skip
     */
    default void onSkipInRead(Throwable t) {}

    /**
     * Called when an item is skipped during the process phase.
     *
     * @param item the item that was skipped
     * @param t    the throwable that caused the skip
     */
    default void onSkipInProcess(T item, Throwable t) {}

    /**
     * Called when an item is skipped during the write phase.
     *
     * @param item the item that was skipped
     * @param t    the throwable that caused the skip
     */
    default void onSkipInWrite(S item, Throwable t) {}
}
