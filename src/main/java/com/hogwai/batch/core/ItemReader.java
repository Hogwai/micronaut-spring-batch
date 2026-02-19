package com.hogwai.batch.core;

/**
 * Strategy interface for providing items one at a time from a data source.
 *
 * @param <T> the type of items to read
 * @see ItemProcessor
 * @see ItemWriter
 */
public interface ItemReader<T> {
    /**
     * Reads the next item from the input source.
     *
     * @return the next item, or {@code null} if the input is exhausted
     */
    T read();
}