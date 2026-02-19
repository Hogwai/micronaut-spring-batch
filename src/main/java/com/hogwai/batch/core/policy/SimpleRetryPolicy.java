package com.hogwai.batch.core.policy;

import java.util.HashSet;
import java.util.Set;

public class SimpleRetryPolicy implements RetryPolicy {
    private final int maxAttempts;
    private final Set<Class<? extends Throwable>> retryableExceptions = new HashSet<>();

    public SimpleRetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void registerRetryableException(Class<? extends Throwable> exceptionClass) {
        retryableExceptions.add(exceptionClass);
    }

    @Override
    public boolean shouldRetry(Throwable t, int attemptCount) {
        if (attemptCount >= maxAttempts) return false;
        if (retryableExceptions.isEmpty()) return true;
        return retryableExceptions.stream().anyMatch(cls -> cls.isInstance(t));
    }
}
