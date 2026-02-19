package com.hogwai.batch.core.policy;

/**
 * Determines whether a failed item should be skipped during chunk processing.
 */
public interface SkipPolicy {

    /**
     * Decides whether the given exception should result in a skip.
     *
     * @param t         the exception thrown during processing
     * @param skipCount the number of items already skipped
     * @return {@code true} if the item should be skipped, {@code false} to propagate the exception
     */
    boolean shouldSkip(Throwable t, long skipCount);
}
