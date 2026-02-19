package com.hogwai.batch.core;

public enum RepeatStatus {
    CONTINUABLE,
    FINISHED;

    public boolean isContinuable() { return this == CONTINUABLE; }
}
