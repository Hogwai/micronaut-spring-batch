package com.hogwai.batch.core.policy;

public interface BackoffPolicy {
    void backoff(int attemptCount) throws InterruptedException;
}
