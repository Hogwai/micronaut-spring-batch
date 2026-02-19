package com.hogwai.batch.core.policy;

/**
 * Determines whether a failed operation should be retried.
 */
public interface RetryPolicy {

    /**
     * Decides whether the operation should be retried after the given failure.
     *
     * @param t            the exception thrown by the failed operation
     * @param attemptCount the number of attempts already made
     * @return {@code true} if the operation should be retried, {@code false} to give up
     */
    boolean shouldRetry(Throwable t, int attemptCount);
}
