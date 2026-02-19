package com.hogwai.batch.core.policy;

import java.util.HashSet;
import java.util.Set;

/**
 * Skip policy that allows skipping up to a configured limit, optionally restricted to specific exception types.
 * If no skippable exception types are registered, all exceptions are considered skippable.
 */
public class SimpleSkipPolicy implements SkipPolicy {
    private final int skipLimit;
    private final Set<Class<? extends Throwable>> skippableExceptions = new HashSet<>();

    /**
     * Creates a skip policy with the given maximum number of allowed skips.
     *
     * @param skipLimit the maximum number of items that can be skipped
     */
    public SimpleSkipPolicy(int skipLimit) {
        this.skipLimit = skipLimit;
    }

    /**
     * Registers an exception type as skippable. Only registered types will be skipped
     * when at least one type is registered.
     *
     * @param exceptionClass the exception type to allow skipping for
     */
    public void registerSkippableException(Class<? extends Throwable> exceptionClass) {
        skippableExceptions.add(exceptionClass);
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldSkip(Throwable t, long skipCount) {
        if (skipCount >= skipLimit) return false;
        if (skippableExceptions.isEmpty()) return true;
        return skippableExceptions.stream().anyMatch(cls -> cls.isInstance(t));
    }
}
