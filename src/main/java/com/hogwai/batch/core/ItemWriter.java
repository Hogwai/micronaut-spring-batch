package com.hogwai.batch.core;

import java.util.List;

/**
 * Writes a chunk of items to an output destination (e.g., database, file, console).
 *
 * @param <O> the type of items to write
 * @see ItemReader
 * @see ItemProcessor
 */
public interface ItemWriter<O> {
    /**
     * Writes the given list of items.
     *
     * @param items the chunk of items to write
     */
    void write(List<O> items);
}
