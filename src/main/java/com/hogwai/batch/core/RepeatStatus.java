package com.hogwai.batch.core;

/**
 * Indicates whether a {@link Tasklet} should continue executing or has finished.
 */
public enum RepeatStatus {
    /** Indicates there is more work to do; the tasklet will be called again. */
    CONTINUABLE,
    /** Indicates the tasklet has completed its work. */
    FINISHED;

    /**
     * Returns {@code true} if this status is {@link #CONTINUABLE}.
     *
     * @return whether execution should continue
     */
    public boolean isContinuable() { return this == CONTINUABLE; }
}
