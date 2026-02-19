package com.hogwai.batch.core.listener;

/**
 * Listener interface for receiving callbacks around chunk boundaries during step execution.
 */
public interface ChunkListener {

    /**
     * Called before a chunk is processed.
     */
    default void beforeChunk() {}

    /**
     * Called after a chunk has been successfully processed and committed.
     */
    default void afterChunk() {}

    /**
     * Called after a chunk fails with an exception.
     *
     * @param exception the exception that caused the chunk to fail
     */
    default void afterChunkError(Exception exception) {}
}
