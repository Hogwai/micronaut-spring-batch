package com.hogwai.batch.core.listener;

public interface ItemProcessListener<I, O> {
    default void beforeProcess(I item) {}
    default void afterProcess(I item, O result) {}
    default void onProcessError(I item, Exception exception) {}
}
