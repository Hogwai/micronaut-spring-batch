package com.hogwai.batch.core.policy;

public class FixedBackoffPolicy implements BackoffPolicy {
    private final long intervalMs;

    public FixedBackoffPolicy(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    @Override
    public void backoff(int attemptCount) throws InterruptedException {
        Thread.sleep(intervalMs);
    }
}
