package com.hogwai.batch.core.policy;

/**
 * Backoff policy that waits a fixed duration between retry attempts, regardless of attempt number.
 */
public class FixedBackoffPolicy implements BackoffPolicy {
    private final long intervalMs;

    /**
     * Creates a fixed backoff policy with the specified interval.
     *
     * @param intervalMs the delay in milliseconds between retries
     */
    public FixedBackoffPolicy(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    /** {@inheritDoc} */
    @Override
    public void backoff(int attemptCount) throws InterruptedException {
        Thread.sleep(intervalMs);
    }
}
