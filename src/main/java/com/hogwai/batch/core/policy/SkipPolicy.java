package com.hogwai.batch.core.policy;

public interface SkipPolicy {
    boolean shouldSkip(Throwable t, long skipCount);
}
