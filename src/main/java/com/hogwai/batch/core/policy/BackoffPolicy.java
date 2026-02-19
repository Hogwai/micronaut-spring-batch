package com.hogwai.batch.core.policy;

/**
 * Defines a delay strategy between retry attempts.
 */
public interface BackoffPolicy {

    /**
     * Pauses execution for the appropriate delay before the next retry attempt.
     *
     * @param attemptCount the current attempt number (starting from 1)
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void backoff(int attemptCount) throws InterruptedException;
}
