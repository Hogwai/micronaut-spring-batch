package com.hogwai.batch.core.policy;

public interface RetryPolicy {
    boolean shouldRetry(Throwable t, int attemptCount);
}
