package com.hogwai.batch.core.listener;

public interface ChunkListener {
    default void beforeChunk() {}
    default void afterChunk() {}
    default void afterChunkError(Exception exception) {}
}
