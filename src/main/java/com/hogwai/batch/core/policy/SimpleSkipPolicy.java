package com.hogwai.batch.core.policy;

import java.util.HashSet;
import java.util.Set;

public class SimpleSkipPolicy implements SkipPolicy {
    private final int skipLimit;
    private final Set<Class<? extends Throwable>> skippableExceptions = new HashSet<>();

    public SimpleSkipPolicy(int skipLimit) {
        this.skipLimit = skipLimit;
    }

    public void registerSkippableException(Class<? extends Throwable> exceptionClass) {
        skippableExceptions.add(exceptionClass);
    }

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) {
        if (skipCount >= skipLimit) return false;
        if (skippableExceptions.isEmpty()) return true;
        return skippableExceptions.stream().anyMatch(cls -> cls.isInstance(t));
    }
}
