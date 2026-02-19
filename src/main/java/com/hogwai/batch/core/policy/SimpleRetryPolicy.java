package com.hogwai.batch.core.policy;

import java.util.HashSet;
import java.util.Set;

/**
 * Retry policy that allows retrying up to a maximum number of attempts, optionally restricted to specific exception types.
 * If no retryable exception types are registered, all exceptions are considered retryable.
 */
public class SimpleRetryPolicy implements RetryPolicy {
    private final int maxAttempts;
    private final Set<Class<? extends Throwable>> retryableExceptions = new HashSet<>();

    /**
     * Creates a retry policy with the given maximum number of attempts.
     *
     * @param maxAttempts the maximum number of retry attempts allowed
     */
    public SimpleRetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    /**
     * Registers an exception type as retryable. Only registered types will trigger retries
     * when at least one type is registered.
     *
     * @param exceptionClass the exception type to allow retries for
     */
    public void registerRetryableException(Class<? extends Throwable> exceptionClass) {
        retryableExceptions.add(exceptionClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldRetry(Throwable t, int attemptCount) {
        if (attemptCount >= maxAttempts) return false;
        if (retryableExceptions.isEmpty()) return true;
        return retryableExceptions.stream().anyMatch(cls -> cls.isInstance(t));
    }
}
